package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.kafka.dto.OrderItemEvent;

public interface StockUpdateService {

    OrderItemEvent updateStock(OrderItemEvent orderItemEvent);
}
