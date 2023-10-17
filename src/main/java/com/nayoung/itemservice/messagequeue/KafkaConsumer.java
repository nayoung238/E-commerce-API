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

    @KafkaListener(topics = KafkaConsumerConfig.TEMPORARY_ORDER_TOPIC_NAME)
    public void updateStock(OrderDto orderDto) {
        log.info("Consuming message Success -> eventId:{}", orderDto.getEventId());

        for(OrderItemDto orderItemDto : orderDto.getOrderItemDtos())
            orderItemDto.convertSign();

        itemStockService.updateStock(orderDto);
    }
}
