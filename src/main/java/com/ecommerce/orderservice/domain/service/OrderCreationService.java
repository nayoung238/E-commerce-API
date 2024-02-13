package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.web.dto.OrderDto;
import org.apache.kafka.clients.consumer.ConsumerRecord;

public interface OrderCreationService {

    OrderDto create(OrderDto orderDto);
    void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record);
    void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record);
}
