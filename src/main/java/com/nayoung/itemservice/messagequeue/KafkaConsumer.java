package com.nayoung.itemservice.messagequeue;

import com.nayoung.itemservice.domain.item.ItemStockService;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final ItemStockService itemStockService;

    @KafkaListener(topics = "e-commerce.order.order-details")
    public void updateStock(OrderDto order) {
        itemStockService.updateStock(order);
    }
}
