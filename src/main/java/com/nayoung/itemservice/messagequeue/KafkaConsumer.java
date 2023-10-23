package com.nayoung.itemservice.messagequeue;

import com.nayoung.itemservice.domain.item.ItemStockService;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import com.nayoung.itemservice.messagequeue.client.OrderItemDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ItemStockService itemStockService;

    @KafkaListener(topics = {KafkaConsumerConfig.TEMPORARY_ORDER_TOPIC,
                            KafkaConsumerConfig.TEMPORARY_RETRY_ORDER_TOPIC})
    public void updateStock(OrderDto orderDto) {
        log.info("Consuming message Success -> Topic: {}, Event Id:{}",
                KafkaConsumerConfig.TEMPORARY_ORDER_TOPIC,
                orderDto.getEventId());

        orderDto.getOrderItemDtos()
                .forEach(OrderItemDto::convertSign);
        itemStockService.updateStock(orderDto);
    }
}
