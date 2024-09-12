package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;

public interface StockUpdateService {

    OrderItemKafkaEvent updateStock(OrderItemKafkaEvent orderItemKafkaEvent, ItemProcessingStatus itemProcessingStatus);
}
