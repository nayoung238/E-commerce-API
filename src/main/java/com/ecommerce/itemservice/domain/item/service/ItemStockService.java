package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.service.stockupdate.ItemUpdateStatus;
import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.domain.item.repository.OrderRedisRepository;
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
            if(orderKafkaEvent.getOrderStatus() == OrderStatus.WAITING) {
                processFirstEvent(orderKafkaEvent, ItemUpdateStatus.STOCK_CONSUMPTION);
            }
            else if(orderKafkaEvent.getOrderStatus() == OrderStatus.CANCELED) {
                processFirstEvent(orderKafkaEvent, ItemUpdateStatus.STOCK_PRODUCTION);
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

    private void processFirstEvent(OrderKafkaEvent orderKafkaEvent, ItemUpdateStatus itemUpdateStatus) {
        List<OrderItemKafkaEvent> result = orderKafkaEvent.getOrderItemKafkaEvents()
                .stream()
                .map(o -> stockUpdateService.updateStock(o, itemUpdateStatus))
                .toList();

        if (isAllSucceeded(result)) {
            orderKafkaEvent.updateOrderStatus(OrderStatus.SUCCEEDED);
        } else {
            if(itemUpdateStatus == ItemUpdateStatus.STOCK_CONSUMPTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemUpdateStatus.STOCK_PRODUCTION);
            }
            else if(itemUpdateStatus == ItemUpdateStatus.STOCK_PRODUCTION) {
                handleFailedUpdate(orderKafkaEvent, result, ItemUpdateStatus.STOCK_CONSUMPTION);
            }
        }
        orderRedisRepository.setOrderStatus(orderKafkaEvent.getOrderEventId(), orderKafkaEvent.getOrderStatus());
    }

    private boolean isAllSucceeded(List<OrderItemKafkaEvent> orderItemKafkaEvents) {
        return orderItemKafkaEvents.stream()
                .allMatch(o -> Objects.equals(OrderStatus.SUCCEEDED, o.getOrderStatus()));
    }

    private void handleFailedUpdate(OrderKafkaEvent orderKafkaEvent, List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemUpdateStatus itemUpdateStatus) {
        undo(orderItemKafkaEvents, itemUpdateStatus);
        orderKafkaEvent.updateOrderStatus(OrderStatus.FAILED);
        orderKafkaEvent.updateOrderItemDtos(orderItemKafkaEvents);
    }

    private void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        String orderProcessingResult = orderRedisRepository.getOrderStatus(orderKafkaEvent.getOrderEventId());

        OrderStatus orderStatus;
        if(orderProcessingResult == null) orderStatus = OrderStatus.NOT_EXIST;
        else orderStatus = OrderStatus.getStatus(orderProcessingResult);

        orderKafkaEvent.updateOrderStatus(orderStatus);
        orderKafkaEvent.getOrderItemKafkaEvents()
                .forEach(orderItemDto -> orderItemDto.updateOrderStatus(orderStatus));
    }

    private void undo(List<OrderItemKafkaEvent> orderItemKafkaEvents, ItemUpdateStatus itemUpdateStatus) {
        orderItemKafkaEvents.stream()
                .filter(o -> Objects.equals(OrderStatus.SUCCEEDED, o.getOrderStatus()))
                .forEach(o -> {
                    stockUpdateService.updateStock(o, itemUpdateStatus);
                    o.updateOrderStatus(OrderStatus.CANCELED);
                });
    }

    @Transactional
    public void updateStockWithPessimisticLock(Long itemId, Long quantity, ItemUpdateStatus itemUpdateStatus) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ITEM.getMessage()));

        if(quantity < 0) quantity *= -1;
        if(itemUpdateStatus == ItemUpdateStatus.STOCK_CONSUMPTION) {
            item.decreaseStock(quantity);
        }
        else if(itemUpdateStatus == ItemUpdateStatus.STOCK_PRODUCTION) {
            item.increaseStock(quantity);
        }
    }
}
