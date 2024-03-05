package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.exception.ExceptionCode;
import com.ecommerce.itemservice.exception.ItemException;
import com.ecommerce.itemservice.kafka.dto.OrderDto;
import com.ecommerce.itemservice.kafka.dto.OrderItemDto;
import com.ecommerce.itemservice.kafka.dto.OrderItemStatus;
import com.ecommerce.itemservice.kafka.config.producer.KafkaProducerConfig;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.domain.item.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public void updateStock(OrderDto orderDto) {
        if (isFirstEvent(orderDto)) {  // 최초 요청만 재고 변경 진행
            List<OrderItemDto> result = orderDto.getOrderItemDtos()
                    .stream()
                    .map(o -> {
                        o.convertSign();
                        return stockUpdateService.updateStock(o, orderDto.getEventId());
                    })
                    .toList();

            if (isAllSucceeded(result)) {
                orderDto.updateOrderStatus(OrderItemStatus.SUCCEEDED);
            } else {
                orderDto.updateOrderStatus(OrderItemStatus.FAILED);
                List<OrderItemDto> orderItemDtoList = undo(orderDto.getEventId(), result);
                orderDto.setOrderItemDtos(orderItemDtoList);
            }
            orderRedisRepository.setOrderStatus(orderDto.getEventId(), orderDto.getOrderStatus());
            kafkaProducerService.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC, orderDto.getEventId(), orderDto);
        }
        else {
            updateOrderStatus(orderDto);
            kafkaProducerService.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC, orderDto.getEventId(), orderDto);
        }
    }

    private boolean isFirstEvent(OrderDto orderDto) {
        String redisKey = getRedisKey(orderDto);
        return orderRedisRepository.addEventId(redisKey, orderDto.getEventId()) == 1;
    }

    private String getRedisKey(OrderDto orderDto) {
        String[] keys;
        if(orderDto.getCreatedAt() != null)
            keys = orderDto.getCreatedAt().toString().split(":");
        else
            keys = orderDto.getRequestedAt().toString().split(":");

        return keys[0]; // yyyy-mm-dd'T'HH
    }

    private boolean isAllSucceeded(List<OrderItemDto> orderItemDtos) {
        return orderItemDtos.stream()
                .allMatch(o -> Objects.equals(OrderItemStatus.SUCCEEDED, o.getOrderItemStatus()));
    }

    private void updateOrderStatus(OrderDto orderDto) {
        String orderProcessingResult = orderRedisRepository.getOrderStatus(orderDto.getEventId());

        OrderItemStatus orderItemStatus;
        if(orderProcessingResult == null) orderItemStatus = OrderItemStatus.NOT_EXIST;
        else orderItemStatus = OrderItemStatus.getOrderItemStatus(orderProcessingResult);

        orderDto.updateOrderStatus(orderItemStatus);
        orderDto.getOrderItemDtos()
                .forEach(orderItemDto -> orderItemDto.setOrderItemStatus(orderItemStatus));
    }

    private List<OrderItemDto> undo(String eventId, List<OrderItemDto> orderItemDtos) {
        orderItemDtos.stream()
                .filter(o -> Objects.equals(OrderItemStatus.SUCCEEDED, o.getOrderItemStatus()))
                .forEach(o -> {
                    o.convertSign();
                    stockUpdateService.updateStock(o, eventId);
                    o.setOrderItemStatus(OrderItemStatus.CANCELED);
                });

        return orderItemDtos;
    }

    @Transactional
    public void updateStockWithPessimisticLock(Long itemId, Long quantity) {
        Item item = itemRepository.findByIdWithPessimisticLock(itemId)
                .orElseThrow(() -> new ItemException(ExceptionCode.NOT_FOUND_ITEM));

        item.updateStock(quantity);
    }
}
