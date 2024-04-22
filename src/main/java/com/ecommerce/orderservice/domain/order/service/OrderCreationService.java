package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;

import java.util.UUID;

public interface OrderCreationService {

    OrderDto create(OrderDto orderDto);
    void checkFinalStatusOfOrder(ConsumerRecord<String, OrderKafkaEvent> record);
    void requestOrderProcessingResult(ConsumerRecord<String, OrderKafkaEvent> record);

    default String createOrderEventId(Long userId) {
        if(userId == null) {
            throw new OrderException(ExceptionCode.NOT_NULL_USER_ID);
        }
        String[] uuid = UUID.randomUUID().toString().split("-");
        return userId + "-" + uuid[0];
    }
}
