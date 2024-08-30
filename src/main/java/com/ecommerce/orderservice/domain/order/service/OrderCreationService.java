package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

public interface OrderCreationService {

    OrderDto create(OrderRequestDto orderRequestDto);
    void checkFinalStatusOfOrder(ConsumerRecord<String, OrderKafkaEvent> record);
    void requestOrderProcessingResult(ConsumerRecord<String, OrderKafkaEvent> record);

    default String getOrderEventId(Long accountId) {
        if(accountId == null)
            throw new IllegalArgumentException("accountId cannot be null");

        String[] uuid = UUID.randomUUID().toString().split("-");
        return accountId + "-" + uuid[0];
    }
}
