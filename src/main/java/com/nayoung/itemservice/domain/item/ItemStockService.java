package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.StockException;
import com.nayoung.itemservice.messagequeue.KStreamConfig;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final ItemRedisRepository itemRedisRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final KafkaTemplate<String, Long> kafkaTemplate;

    /**
     * Exclusive Lock 사용
     * 모든 요청이 X-Lock을 획득하기 위해 대기 -> 대기로 인해 지연 시간 발생
     * 재고 변경 작업의 지연은 주문 상태 확정의 지연으로 이어짐
     *
     * DB X-Lock을 획득한 노드가 죽는 경우 락을 자동 반납하지 않아 다른 요청은 무한정 대기할 수 있음
     * -> Redis Distributed lock에 lease time 설정하는 방식으로 해결 (updateStockByRedisson method)
     */
    @Transactional
    public OrderItemStatus updateStockByPessimisticLock(Long orderId, Long customerAccountId, Long itemId, Long quantity) {
        OrderItemStatus orderItemStatus;
        try {
            Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                    .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
            item.updateStock(quantity);
            orderItemStatus = OrderItemStatus.SUCCEEDED;
        } catch (ItemException e) {
            orderItemStatus = OrderItemStatus.FAILED;
        } catch(StockException e) {
            orderItemStatus = OrderItemStatus.OUT_OF_STOCK;
        }

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderId, customerAccountId, itemId, quantity);
        itemUpdateLogRepository.save(itemUpdateLog);
        return itemUpdateLog.getOrderItemStatus();
    }

    /**
     * Redis Distributed Lock + Optimistic Lock 사용
     * Redis 분산락을 획득한 요청만이 DB에 접근해 수정할 수 있음
     * 분산락에 lease time 설정 -> DB 락을 획득한 노드가 죽는 경우 발생할 수 있는 문제 해결 (활성 상태가 될 때까지 모든 요청 대기해야 하는 문제)
     *
     * 분산락 lease time 보다 transaction 처리가 더 길다면 동시성 문제 발생할 수 있음 (여러 요청이 자신이 분산락 주인이라고 착각하고 쿼리 날리는 경우)
     * -> Optimistic Lock을 사용해 DB 반영 시 충돌 감지해 동시성 문제 해결
     */
    @Transactional
    public OrderItemStatus updateStockByRedisson(Long orderId, Long customerAccountId, Long itemId, Long quantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        OrderItemStatus orderItemStatus;
        if(isUpdatableStockByRedis(item.getId(), quantity)) {
            orderItemStatus = (quantity < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED;  // undo 작업에서 발생하는 production
        }
        else orderItemStatus = OrderItemStatus.OUT_OF_STOCK;

        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderId, customerAccountId, itemId, quantity);
        itemUpdateLogRepository.save(itemUpdateLog);
        return itemUpdateLog.getOrderItemStatus();
    }

    /**
     * Kafka KStream을 이용해 집계하는 방식
     * 재고 변경 로그를 KStream(Key: Item ID, Value: Quantity)에 추가
     * groupByKey로 합계를 구해 DB에 반영 (여러 변경 요청을 모아 한 번에 DB에 반영)
     * 집계하는 과정에서 이벤트가 중복되지 않게 Tumbling window 사용
     */
    public OrderItemStatus updateStockByKStream(Long orderId, Long customerAccountId, Long itemId, Long quantity) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        // Redis에서 재고 차감 시도
        OrderItemStatus orderItemStatus;
        if(isUpdatableStockByRedis(item.getId(), quantity)) {
            orderItemStatus = (quantity < 0) ?
                    OrderItemStatus.SUCCEEDED  // consumption
                    : OrderItemStatus.CANCELED;  // undo 작업에서 발생하는 production
        }
        else orderItemStatus = OrderItemStatus.OUT_OF_STOCK;

        // undo 작업 판별하기 위해 DB에 기록
        ItemUpdateLog itemUpdateLog = ItemUpdateLog.from(orderItemStatus, orderId, customerAccountId, itemId, quantity);
        itemUpdateLogRepository.save(itemUpdateLog);

        /*
            consumption(SUCCEEDED)이거나 UNDO 작업에서 발생하는 production(CANCELED)이면 재고가 변경되어야 하므로
            KStream에 재고 변경 데이터 추가
         */
        if(Objects.equals(OrderItemStatus.SUCCEEDED, itemUpdateLog.getOrderItemStatus())
                || Objects.equals(OrderItemStatus.CANCELED, itemUpdateLog.getOrderItemStatus())) {
            final Long itemUpdateLogId = itemUpdateLog.getId();
            sendMessageToKStream(itemId, quantity, itemUpdateLogId);
        }

        return itemUpdateLog.getOrderItemStatus();
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
            kafkaTemplate.send(KStreamConfig.ITEM_LOG_TOPIC, String.valueOf(itemId), quantity)
                    .addCallback(result -> {
                        assert result != null;

                        // broker에 log 적재한 후의 시간 기록
                        setLogCreatedAt(itemUpdateLogId);

                        RecordMetadata metadata = result.getRecordMetadata();
                        log.info("Producing message Success topic {} partition {} offset {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset());
                    }, exception -> log.error("Producing message Failure " + exception.getMessage()));
        } catch (KafkaProducerException e) {
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
}
