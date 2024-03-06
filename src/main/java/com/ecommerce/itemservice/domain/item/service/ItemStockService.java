package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.exception.ItemException;
import com.ecommerce.itemservice.kafka.dto.OrderEvent;
import com.ecommerce.itemservice.kafka.dto.OrderItemEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.domain.item.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

import static com.ecommerce.itemservice.kafka.config.producer.KafkaProducerConfig.ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC;
import static com.ecommerce.itemservice.kafka.config.producer.KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OrderRedisRepository orderRedisRepository;
    private final StockUpdateService stockUpdateService;

    @Transactional
    public void updateStock(OrderEvent orderEvent, boolean isStreamsOnly) {
        if (isFirstEvent(orderEvent)) {  // 최초 요청만 재고 변경 진행
            List<OrderItemEvent> result = orderEvent.getOrderItemEvents()
                    .stream()
                    .map(o -> {
                        o.convertSign();
                        return stockUpdateService.updateStock(o);
                    })
                    .toList();

            if (isAllSucceeded(result)) {
                orderEvent.updateOrderStatus(OrderStatus.SUCCEEDED);
            } else {
                List<OrderItemEvent> orderItemEvents = undo(result);
                orderEvent.updateOrderStatus(OrderStatus.FAILED);
                orderEvent.updateOrderItemDtos(orderItemEvents);
            }
            orderRedisRepository.setOrderStatus(orderEvent.getOrderEventKey(), orderEvent.getOrderStatus());
            String topic = (isStreamsOnly) ? ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC : ITEM_UPDATE_RESULT_TOPIC;
            kafkaProducerService.sendMessage(topic, orderEvent.getOrderEventKey(), orderEvent);
        }
        else {
            updateOrderStatus(orderEvent);
            String topic = (isStreamsOnly) ? ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC : ITEM_UPDATE_RESULT_TOPIC;
            kafkaProducerService.sendMessage(topic, orderEvent.getOrderEventKey(), orderEvent);
        }
    }

    private boolean isFirstEvent(OrderEvent orderEvent) {
        String redisKey = getRedisKey(orderEvent);
        return orderRedisRepository.addEventId(redisKey, orderEvent.getOrderEventKey()) == 1;
    }

    private String getRedisKey(OrderEvent orderEvent) {
        String[] keys;
        if(orderEvent.getCreatedAt() != null)
            keys = orderEvent.getCreatedAt().toString().split(":");
        else
            keys = orderEvent.getRequestedAt().toString().split(":");

        return keys[0]; // yyyy-mm-dd'T'HH
    }

    private boolean isAllSucceeded(List<OrderItemEvent> orderItemEvents) {
        return orderItemEvents.stream()
                .allMatch(o -> Objects.equals(OrderStatus.SUCCEEDED, o.getOrderStatus()));
    }

    private void updateOrderStatus(OrderEvent orderEvent) {
        String orderProcessingResult = orderRedisRepository.getOrderStatus(orderEvent.getOrderEventKey());

        OrderStatus orderStatus;
        if(orderProcessingResult == null) orderStatus = OrderStatus.NOT_EXIST;
        else orderStatus = OrderStatus.getStatus(orderProcessingResult);

        orderEvent.updateOrderStatus(orderStatus);
        orderEvent.getOrderItemEvents()
                .forEach(orderItemDto -> orderItemDto.updateOrderStatus(orderStatus));
    }

    private List<OrderItemEvent> undo(List<OrderItemEvent> orderItemEvents) {
        orderItemEvents.stream()
                .filter(o -> Objects.equals(OrderStatus.SUCCEEDED, o.getOrderStatus()))
                .forEach(o -> {
                    o.convertSign();
                    stockUpdateService.updateStock(o);
                    o.updateOrderStatus(OrderStatus.CANCELED);
                });

        return orderItemEvents;
    }

    @Transactional
    public void updateStockWithPessimisticLock(Long itemId, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        item.updateStock(quantity);
    }
}
