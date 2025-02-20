package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.common.exception.ExceptionCode;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.repository.OrderRedisRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemStockService {

    private final ItemRepository itemRepository;
    private final KafkaProducerService kafkaProducerService;
    private final OrderRedisRepository orderRedisRepository;
    private final StockUpdateService stockUpdateService;

    @Transactional
    public void updateStock(OrderKafkaEvent orderKafkaEvent, boolean isStreamsOnly) {
        if (isFirstEvent(orderKafkaEvent)) {  // 최초 요청만 재고 변경 진행
            if(orderKafkaEvent.getOrderProcessingStatus() == OrderProcessingStatus.PROCESSING) {
                processFirstEvent(orderKafkaEvent, ItemProcessingStatus.STOCK_CONSUMPTION);
            }
            else if(orderKafkaEvent.getOrderProcessingStatus() == OrderProcessingStatus.CANCELED) {
                processFirstEvent(orderKafkaEvent, ItemProcessingStatus.STOCK_PRODUCTION);
            }
        }
        else {  // 최초 요청이 아니면 결과만 반환
            updateOrderStatus(orderKafkaEvent);
        }

        String topic = (isStreamsOnly) ? TopicConfig.ITEM_UPDATE_RESULT_STREAMS_ONLY_TOPIC : TopicConfig.ITEM_UPDATE_RESULT_TOPIC;
        kafkaProducerService.sendMessage(topic, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
    }

    private boolean isFirstEvent(OrderKafkaEvent orderKafkaEvent) {
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
            orderKafkaEvent.updateOrderProcessingStatus(OrderProcessingStatus.SUCCESSFUL);
        } else {
            if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemProcessingStatus.STOCK_PRODUCTION);
            }
            else if(itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemProcessingStatus.STOCK_CONSUMPTION);
            }
        }
        orderRedisRepository.setOrderProcessingStatus(orderKafkaEvent.getOrderEventId(), orderKafkaEvent.getOrderProcessingStatus());
    }

    private boolean isAllSucceeded(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        return orderItemKafkaEvents.stream()
                .allMatch(o -> Objects.equals(OrderProcessingStatus.SUCCESSFUL, o.getOrderProcessingStatus()));
    }

    private void handleFailedUpdate(OrderKafkaEvent orderKafkaEvent, List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemProcessingStatus itemProcessingStatus) {
        undo(orderItemKafkaEvents, itemProcessingStatus);
        orderKafkaEvent.updateOrderProcessingStatus(OrderProcessingStatus.FAILED);
        orderKafkaEvent.updateOrderItemDtos(orderItemKafkaEvents);
    }

    private void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        String orderProcessingResult = orderRedisRepository.getOrderProcessingStatus(orderKafkaEvent.getOrderEventId());

        OrderProcessingStatus orderProcessingStatus;
        if(orderProcessingResult == null) orderProcessingStatus = OrderProcessingStatus.NOT_EXIST;
        else orderProcessingStatus = OrderProcessingStatus.getStatus(orderProcessingResult);

        orderKafkaEvent.updateOrderProcessingStatus(orderProcessingStatus);
        orderKafkaEvent.getOrderItemKafkaEvents()
                .forEach(o -> o.updateOrderProcessingStatus(orderProcessingStatus));
    }

    private void undo(List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemProcessingStatus itemProcessingStatus) {
        orderItemKafkaEvents.stream()
                .filter(o -> Objects.equals(OrderProcessingStatus.SUCCESSFUL, o.getOrderProcessingStatus()))
                .forEach(o -> {
                    stockUpdateService.updateStock(o, itemProcessingStatus);
                    o.updateOrderProcessingStatus(OrderProcessingStatus.CANCELED);
                });
    }

    @Transactional
    public void updateStockWithPessimisticLock(Long itemId, Long quantity, ItemProcessingStatus itemProcessingStatus) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ITEM.getMessage()));

        if(quantity < 0) quantity *= -1;
        if(itemProcessingStatus == ItemProcessingStatus.STOCK_CONSUMPTION) {
            item.decreaseStock(quantity);
        }
        else if(itemProcessingStatus == ItemProcessingStatus.STOCK_PRODUCTION) {
            item.increaseStock(quantity);
        }
    }
}
