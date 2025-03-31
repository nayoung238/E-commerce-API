package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.common.exception.ErrorCode;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.KafkaProducerService;
import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.repository.OrderRedisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OrderRedisRepository orderRedisRepository;
    private final StockUpdateService stockUpdateService;

    @Transactional
    public void updateStock(OrderKafkaEvent orderKafkaEvent, boolean isStreamsOnly) {
        if (isInitialEvent(orderKafkaEvent)) {  // 최초 요청만 재고 변경 진행
            if(orderKafkaEvent.getOrderStatus() == OrderStatus.PROCESSING) {
                processFirstEvent(orderKafkaEvent, ItemProcessingStatus.STOCK_CONSUMPTION);
            }
            else if(orderKafkaEvent.getOrderStatus() == OrderStatus.CANCELED) {
                processFirstEvent(orderKafkaEvent, ItemProcessingStatus.STOCK_PRODUCTION);
            }
        }
        else {  // 최초 요청이 아니면 결과만 반환
            updateOrderStatus(orderKafkaEvent);
        }

        String topic = (isStreamsOnly) ? TopicConfig.ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC : TopicConfig.ITEM_UPDATE_RESULT_TOPIC;
        kafkaProducerService.sendMessage(topic, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
    }

    private boolean isInitialEvent(OrderKafkaEvent orderKafkaEvent) {
        String redisKey = getRedisKey(orderKafkaEvent);
        return orderRedisRepository.addEventId(redisKey, orderKafkaEvent.getOrderEventId()) == 1;
    }

    private String getRedisKey(OrderKafkaEvent orderKafkaEvent) {
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd:HH");
        return orderKafkaEvent.getRequestedAt().format(formatter);
    }

    private void processFirstEvent(OrderKafkaEvent orderKafkaEvent, ItemProcessingStatus itemProcessingStatus) {
        List<OrderItemKafkaEvent> result = orderKafkaEvent.getOrderItemKafkaEvents()
                .stream()
                .map(o -> stockUpdateService.updateStock(o, itemProcessingStatus))
                .toList();

        if (isAllSucceeded(result)) {
            orderKafkaEvent.updateOrderProcessingStatus(OrderStatus.SUCCESSFUL);
        } else {
            if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemProcessingStatus.STOCK_PRODUCTION);
            }
            else if(itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemProcessingStatus.STOCK_CONSUMPTION);
            }
        }
        orderRedisRepository.setOrderProcessingStatus(orderKafkaEvent.getOrderEventId(), orderKafkaEvent.getOrderStatus());
    }

    private boolean isAllSucceeded(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        return orderItemKafkaEvents.stream()
                .allMatch(o -> Objects.equals(OrderStatus.SUCCESSFUL, o.getOrderStatus()));
    }

    private void handleFailedUpdate(OrderKafkaEvent orderKafkaEvent, List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemProcessingStatus itemProcessingStatus) {
        undo(orderItemKafkaEvents, itemProcessingStatus);
        orderKafkaEvent.updateOrderProcessingStatus(OrderStatus.FAILED);
        orderKafkaEvent.updateOrderItemDtos(orderItemKafkaEvents);
    }

    private void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        String orderProcessingResult = orderRedisRepository.getOrderProcessingStatus(orderKafkaEvent.getOrderEventId());

        OrderStatus orderStatus;
        if(orderProcessingResult == null) orderStatus = OrderStatus.NOT_EXIST;
        else orderStatus = OrderStatus.getStatus(orderProcessingResult);

        orderKafkaEvent.updateOrderProcessingStatus(orderStatus);
        orderKafkaEvent.getOrderItemKafkaEvents()
                .forEach(o -> o.updateOrderStatus(orderStatus));
    }

    private void undo(List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemProcessingStatus itemProcessingStatus) {
        orderItemKafkaEvents.stream()
                .filter(o -> Objects.equals(OrderStatus.SUCCESSFUL, o.getOrderStatus()))
                .forEach(o -> {
                    stockUpdateService.updateStock(o, itemProcessingStatus);
                    o.updateOrderStatus(OrderStatus.CANCELED);
                });
    }

    @Transactional
    public void updateStockWithPessimisticLock(Long itemId, Long quantity, ItemProcessingStatus itemProcessingStatus) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ITEM.getMessage()));

        if(quantity < 0) quantity *= -1;
        if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
            item.decreaseStock(quantity);
        }
        else if(itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
            item.increaseStock(quantity);
        }
    }
}
