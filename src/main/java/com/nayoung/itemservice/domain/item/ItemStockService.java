package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.messagequeue.KafkaProducer;
import com.nayoung.itemservice.messagequeue.KafkaProducerConfig;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final KafkaProducer kafkaProducer;
    private final OrderRedisRepository orderRedisRepository;
    private final RedissonClient redissonClient;
    private final String REDISSON_ITEM_LOCK_PREFIX = "ITEM:";

    /**
     * producer에서 이벤트 유실이라 판단하면 재시도 대상이라 판단해 재전송함
     * 만약 이벤트가 유실되지 않았는데 같은 주문에 대한 이벤트가 재전송되면 consumer는 같은 주문을 중복 처리하게 됨
     * (이벤트 유실에 대한 원인을 제대로 파악할 수 없어서 이미 처리한 이벤트가 재시도 대상이 될 수 있음)
     *
     * 중복 처리를 막기 위해 redis에서 이미 처리된 주문 이벤트인지 먼저 파악 (order ID를 멱등키로 사용)
     */
    @Transactional
    public void updateStock(OrderDto orderDto) {
        /*
            Redis에서 order:yyyy-mm-dd'T'HH(key)애 eventId(value)의 존재 여부 파악
            addEventId method로 Redis에 eventID를 추가했을 때 1을 return 받아야 최초 요청
         */
        String[] redisKey = orderDto.getCreatedAt().toString().split(":");  // key -> order:yyyy-mm-dd'T'HH
        if(orderRedisRepository.addEventId(redisKey[0], orderDto.getEventId()) == 1) {
            List<OrderItemDto> result = orderDto.getOrderItemDtos().stream()
                    .filter(orderItem -> orderItem.getQuantity() < 0L)
                    //.map(o -> updateStockByPessimisticLock(o, orderDto.getId(), orderDto.getEventId())
                    //.map(o -> getDistributedLockAndUpdateStock(o, orderDto.getId(), orderDto.getEventId())
                    .map(o -> updateStockByKStream(o, orderDto.getId(), orderDto.getEventId()))
                    .collect(Collectors.toList());

            boolean isAllSucceeded = result.stream()
                    .allMatch(o -> Objects.equals(OrderItemStatus.SUCCEEDED, o.getOrderItemStatus()));

            if(isAllSucceeded)
                orderDto.setOrderStatus(OrderItemStatus.SUCCEEDED);
            else {
                orderDto.setOrderStatus(OrderItemStatus.FAILED);
                undo(orderDto.getId(), orderDto.getEventId());

                List<OrderItemDto> orderItemDtos = itemUpdateLogRepository.findAllByEventId(orderDto.getEventId())
                        .stream()
                        .map(OrderItemDto::from)
                        .collect(Collectors.toList());

                orderDto.setOrderItemDtos(orderItemDtos);
            }
            kafkaProducer.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC_NAME, orderDto.getEventId(), orderDto);
        }
    }

    /**
     * Exclusive Lock 사용
     * 모든 요청이 X-Lock을 획득하기 위해 대기 -> 대기로 인해 지연 시간 발생
     * 재고 변경 작업의 지연은 주문 상태 확정의 지연으로 이어짐
     *
     * DB X-Lock을 획득한 노드가 죽는 경우 락을 자동 반납하지 않아 다른 요청은 무한정 대기할 수 있음
     * -> Redis Distributed lock에 lease time 설정하는 방식으로 해결 (updateStockByRedisson method)
     */
    private OrderItemDto updateStockByPessimisticLock(OrderItemDto orderItemDto, Long orderId, String eventId) {
        OrderItemStatus orderItemStatus;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(orderItemDto.getItemId())
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.updateStock(orderItemDto.getQuantity());
            orderItemStatus = OrderItemStatus.SUCCEEDED;
        } catch (ItemException e) {
            orderItemStatus = OrderItemStatus.FAILED;
        } catch(StockException e) {
            orderItemStatus = OrderItemStatus.OUT_OF_STOCK;
        }

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, orderId, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

        orderItemDto.setOrderItemStatus(orderItemStatus);
        return orderItemDto;
    }

    /**
     * Redis Distributed Lock + Optimistic Lock 사용
     * Redis 분산락을 획득한 요청만이 DB에 접근해 수정할 수 있음
     * 분산락에 lease time 설정 -> DB 락을 획득한 노드가 죽는 경우 발생할 수 있는 문제 해결 (활성 상태가 될 때까지 모든 요청 대기해야 하는 문제)
     *
     * 분산락 lease time 보다 transaction 처리가 더 길다면 동시성 문제 발생할 수 있음 (여러 요청이 자신이 분산락 주인이라고 착각하고 쿼리 날리는 경우)
     * -> Optimistic Lock을 사용해 DB 반영 시 충돌 감지해 동시성 문제 해결
     */
    private OrderItemDto getDistributedLockAndUpdateStock(OrderItemDto orderItemDto, Long orderId, String eventId) {
        RLock lock = redissonClient.getLock(generateKey(orderItemDto.getItemId()));
        try {
            boolean available = lock.tryLock(10, 1, TimeUnit.SECONDS);
            if(!available) {
                log.error("Lock 획득 실패");
                orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
            }
            return updateStockByRedisson(orderItemDto, orderId, eventId);
        } catch (InterruptedException e) {
            e.printStackTrace();
            orderItemDto.setOrderItemStatus(OrderItemStatus.FAILED);
        } finally {
            lock.unlock();
        }
        return orderItemDto;
    }

    private String generateKey(Long key) {
        return REDISSON_ITEM_LOCK_PREFIX + key.toString();
    }

    private OrderItemDto updateStockByRedisson(OrderItemDto orderItemDto, Long orderId, String eventId) {
        Item item = itemRepository.findById(orderItemDto.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        OrderItemStatus orderItemStatus;
        if(isUpdatableStockByRedis(item.getId(), orderItemDto.getQuantity())) {
            orderItemStatus = (orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED;  // undo 작업에서 발생하는 production
        }
        else orderItemStatus = OrderItemStatus.OUT_OF_STOCK;

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, orderId, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

        orderItemDto.setOrderItemStatus(orderItemStatus);
        return orderItemDto;
    }

    /**
     * Kafka KStream을 이용해 집계하는 방식
     * 재고 변경 로그를 KStream(Key: Item ID, Value: Quantity)에 추가
     * groupByKey로 합계를 구해 DB에 반영 (여러 변경 요청을 모아 한 번에 DB에 반영)
     * 집계하는 과정에서 이벤트가 중복되지 않게 Tumbling window 사용
     */
    private OrderItemDto updateStockByKStream(OrderItemDto orderItemDto, Long orderId, String eventId) {
        Item item = itemRepository.findById(orderItemDto.getItemId())
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        OrderItemStatus orderItemStatus;
        if(isUpdatableStockByRedis(item.getId(), orderItemDto.getQuantity())) {
            orderItemStatus = (orderItemDto.getQuantity() < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED;  // undo 작업에서 발생하는 production
        }
        else orderItemStatus = OrderItemStatus.OUT_OF_STOCK;

        // undo 작업 판별하기 위해 DB에 기록
        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderItemDto, orderId, eventId);
        itemUpdateLogRepository.save(itemUpdateLog);

        /*
            consumption(SUCCEEDED)이거나 UNDO 작업에서 발생하는 production(CANCELED)이면 재고가 변경되어야 하므로
            KStream에 재고 변경 데이터 추가
         */
        if(Objects.equals(OrderItemStatus.SUCCEEDED, itemUpdateLog.getOrderItemStatus())
                || Objects.equals(OrderItemStatus.CANCELED, itemUpdateLog.getOrderItemStatus())) {
            final Long itemUpdateLogId = itemUpdateLog.getId();
            sendMessageToKStream(orderItemDto.getItemId(), orderItemDto.getQuantity(), itemUpdateLogId);
        }

        orderItemDto.setOrderItemStatus(orderItemStatus);
        return orderItemDto;
    }

    private boolean isUpdatableStockByRedis(Long itemId, Long quantity) {
        Long stock = itemRedisRepository.incrementItemStock(itemId, quantity);
        if(stock >= 0) return true;

        // undo
        itemRedisRepository.decrementItemStock(itemId, quantity);
        return false;
    }

    private void sendMessageToKStream(Long itemId, Long quantity, Long itemUpdateLogId) {
        try {
            kafkaProducer.sendMessage(KafkaProducerConfig.ITEM_LOG_TOPIC, String.valueOf(itemId), quantity);
            setLogCreatedAt(itemUpdateLogId);  // broker에 log 적재한 후의 시간 기록
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
            // TODO: broker에 적재되지 못하면 logCreatedAt 값이 null -> null 값만 batch 처리
        }
    }

    private void setLogCreatedAt(Long id) {
        ItemUpdateLog itemUpdateLog = itemUpdateLogRepository.findById(id).orElseThrow();
        itemUpdateLog.setLogCreatedAt(LocalDateTime.now());
        itemUpdateLogRepository.save(itemUpdateLog);
    }

    @Transactional
    public void updateStockOnDB(Long itemId, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
        item.updateStock(quantity);
    }

    private void undo(Long orderId, String eventId) {
        List<ItemUpdateLog> itemUpdateLogs;
        if(orderId != null) itemUpdateLogs = itemUpdateLogRepository.findAllByOrderId(orderId);
        else if(eventId != null) itemUpdateLogs = itemUpdateLogRepository.findAllByEventId(eventId);
        else throw new RuntimeException();

        for(ItemUpdateLog itemUpdateLog : itemUpdateLogs) {
            if(Objects.equals(OrderItemStatus.SUCCEEDED, itemUpdateLog.getOrderItemStatus())) {
                try {
                    OrderItemDto orderItemDto = OrderItemDto.from(itemUpdateLog);
                    orderItemDto.convertSign();
                    //updateStockByPessimisticLock(orderItemDto, orderId, eventId);
                    //getDistributedLockAndUpdateStock(orderItemDto, orderId, eventId);
                    updateStockByKStream(orderItemDto, orderId, eventId);
                } catch (ItemException e) {
                    log.error(e.getMessage());
                }
            }
        }
    }
}
