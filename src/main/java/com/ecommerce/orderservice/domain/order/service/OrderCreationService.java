package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface OrderCreationService {

    OrderDto create(OrderDto orderDto);
    void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record);
    void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record);
}
