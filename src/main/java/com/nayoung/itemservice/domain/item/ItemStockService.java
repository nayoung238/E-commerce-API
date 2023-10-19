package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLogRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.messagequeue.KafkaProducer;
import com.nayoung.itemservice.messagequeue.KafkaProducerConfig;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final ItemUpdateLogRepository itemUpdateLogRepository;
    private final KafkaProducer kafkaProducer;
    private final OrderRedisRepository orderRedisRepository;
    private final StockUpdate stockUpdateService;

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
            최초 요청만 재고 변경 작업 진행
         */
        String[] redisKey = orderDto.getCreatedAt().toString().split(":");  // key -> order:yyyy-mm-dd'T'HH
        if(orderRedisRepository.addEventId(redisKey[0], orderDto.getEventId()) == 1) {
            List<OrderItemDto> result = orderDto.getOrderItemDtos().stream()
                    .filter(orderItem -> orderItem.getQuantity() < 0L)
                    .map(o -> stockUpdateService.updateStock(o, orderDto.getId(), orderDto.getEventId()))
                    .collect(Collectors.toList());

            if (!isAllSucceeded(result))
                undo(orderDto.getId(), orderDto.getEventId());
        }

        List<OrderItemDto> orderItemDtos = getOrderItemDtosByEventId(orderDto.getEventId());
        orderDto.setOrderItemDtos(orderItemDtos);

        if(isAllSucceeded(orderDto.getOrderItemDtos())) orderDto.setOrderStatus(OrderItemStatus.SUCCEEDED);
        else orderDto.setOrderStatus(OrderItemStatus.FAILED);

        kafkaProducer.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC_NAME, orderDto.getEventId(), orderDto);
    }

    private boolean isAllSucceeded(List<OrderItemDto> orderItemDtos) {
        return orderItemDtos.stream()
                .allMatch(o -> Objects.equals(OrderItemStatus.SUCCEEDED, o.getOrderItemStatus()));
    }

    private List<OrderItemDto> getOrderItemDtosByEventId(String eventId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByEventId(eventId);

        Set<Long> itemId = new HashSet<>();
        itemUpdateLogs.sort(Comparator.comparing(ItemUpdateLog::getId));
        return itemUpdateLogs.stream()
                .filter(i -> !itemId.contains(i.getItemId()))
                .map(i -> {
                    itemId.add(i.getItemId());
                    return OrderItemDto.from(i);})
                .collect(Collectors.toList());
    }

    @Transactional
    public void updateStockOnDB(Long itemId, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));
        item.updateStock(quantity);
    }

    private void undo(Long orderId, String eventId) {
        List<ItemUpdateLog> itemUpdateLogs;
        if(orderId != null) itemUpdateLogs =  itemUpdateLogRepository.findAllByOrderId(orderId);
        else if(eventId != null) itemUpdateLogs = itemUpdateLogRepository.findAllByEventId(eventId);
        else throw new RuntimeException();

        itemUpdateLogs.stream()
                .filter(i -> Objects.equals(OrderItemStatus.SUCCEEDED, i.getOrderItemStatus()))
                .forEach(i -> {
                    OrderItemDto orderItemDto = OrderItemDto.from(i);
                    orderItemDto.convertSign();
                    stockUpdateService.updateStock(orderItemDto, orderId, eventId);
                });
    }
}
