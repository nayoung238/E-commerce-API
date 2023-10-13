package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.openfeign.ItemServiceClient;
import com.nayoung.orderservice.messagequeue.client.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderService orderService;
    private final ItemServiceClient itemServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @KafkaListener(topics = "e-commerce.order.order-details")
    public void updateOrderStatus(ConsumerRecord<String, OrderDto> record) {
        try {
            waitBasedOnTimestamp(record.timestamp());

            OrderDto orderDto = record.value();
            updateOrderStatus(orderDto.getId());
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitBasedOnTimestamp(long recordTimestamp) throws InterruptedException {
        Instant recordAppendTime = Instant.ofEpochMilli(recordTimestamp);
        Instant now = Instant.now();
        while(now.toEpochMilli() - recordAppendTime.toEpochMilli() < 5000) {
            Thread.sleep(1000);
            now = Instant.now();
        }
    }

    private void updateOrderStatus(Long orderId) {
        try {
            List<ItemUpdateLogDto> itemUpdateLogDtos = getItemUpdateLogDtos(orderId);
            if(itemUpdateLogDtos != null) orderService.updateOrderStatus(itemUpdateLogDtos);
        } catch(FeignException e) {
            e.printStackTrace();
        }
    }

    private List<ItemUpdateLogDto> getItemUpdateLogDtos(Long orderId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        return circuitBreaker.run(() -> itemServiceClient.getItemUpdateLogDtos(orderId), throwable -> null);
    }
}