package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.kafka.dto.OrderItemDto;

public interface StockUpdateService {

    OrderItemDto updateStock(OrderItemDto orderItemDto, String eventId);
}
