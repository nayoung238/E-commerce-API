package com.nayoung.itemservice.domain.item.service;

import com.nayoung.itemservice.domain.item.Item;
import com.nayoung.itemservice.domain.item.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.repository.ItemRepository;
import com.nayoung.itemservice.domain.item.repository.ItemUpdateLogRepository;
import com.nayoung.itemservice.domain.item.repository.OrderRedisRepository;
import com.nayoung.itemservice.exception.ExceptionCode;
import com.nayoung.itemservice.exception.ItemException;
import com.nayoung.itemservice.exception.OrderException;
import com.nayoung.itemservice.kafka.producer.KafkaProducerService;
import com.nayoung.itemservice.kafka.producer.KafkaProducerConfig;
import com.nayoung.itemservice.kafka.dto.OrderDto;
import com.nayoung.itemservice.kafka.dto.OrderItemDto;
import com.nayoung.itemservice.kafka.dto.OrderItemStatus;
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
    private final KafkaProducerService kafkaProducerService;
    private final OrderRedisRepository orderRedisRepository;
    private final StockUpdate stockUpdateService;

    @Transactional
    public void updateStock(OrderDto orderDto) {
        if (isFirstEvent(orderDto)) {  // 최초 요청만 재고 변경 진행
            List<OrderItemDto> result = orderDto.getOrderItemDtos().stream()
                    .filter(orderItem -> orderItem.getQuantity() < 0L)  // consumption
                    .map(o -> stockUpdateService.updateStock(o, orderDto.getEventId()))
                    .collect(Collectors.toList());

            if (isAllSucceeded(result)) {
                orderDto.setOrderStatus(OrderItemStatus.SUCCEEDED);
                orderDto.setOrderItemDtos(result);
            } else {
                orderDto.setOrderStatus(OrderItemStatus.FAILED);
                orderDto.setOrderItemDtos(undo(orderDto.getEventId(), result));
            }
            orderRedisRepository.setOrderStatus(orderDto.getEventId(), orderDto.getOrderStatus());
            kafkaProducerService.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC, orderDto.getEventId(), orderDto);
        }
        else {
            String orderProcessingResult = orderRedisRepository.getOrderStatus(orderDto.getEventId());
            assert orderProcessingResult != null;
            try {
                OrderItemStatus orderItemStatus = OrderItemStatus.getOrderItemStatus(orderProcessingResult);
                orderDto.setOrderStatus(orderItemStatus);
                orderDto.getOrderItemDtos()
                        .forEach(orderItemDto -> orderItemDto.setOrderItemStatus(orderItemStatus));

                kafkaProducerService.sendMessage(KafkaProducerConfig.ITEM_UPDATE_RESULT_TOPIC, orderDto.getEventId(), orderDto);
            } catch (OrderException e) {
                log.error(String.valueOf(e.getExceptionCode()));
            }
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

    private List<OrderItemDto> getOrderItemDtosByEventId(String eventId) {
        List<ItemUpdateLog> itemUpdateLogs = itemUpdateLogRepository.findAllByEventId(eventId);

        Set<Long> itemId = new HashSet<>();
        itemUpdateLogs.sort(Comparator.comparing(ItemUpdateLog::getId).reversed());
        return itemUpdateLogs.stream()
                .filter(i -> !itemId.contains(i.getItemId()))
                .map(i -> {
                    itemId.add(i.getItemId());
                    return OrderItemDto.from(i);})
                .collect(Collectors.toList());
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
