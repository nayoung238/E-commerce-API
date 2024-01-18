package com.nayoung.itemservice.domain.item.service;

import com.nayoung.itemservice.kafka.dto.OrderItemDto;

public interface StockUpdate {

    OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId);
}
