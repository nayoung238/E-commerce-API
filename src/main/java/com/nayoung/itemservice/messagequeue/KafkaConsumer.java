package com.nayoung.itemservice.messagequeue;

import com.nayoung.itemservice.domain.item.RedissonItemService;
import com.nayoung.itemservice.messagequeue.client.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final RedissonItemService redissonItemService;

    @KafkaListener(topics = "e-commerce.order.order-details")
    public void updateStock(OrderDto order) {
        redissonItemService.updateStock(order);
    }
}
