package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;

public interface StockUpdateService {

    OrderItemKafkaEvent updateStock(OrderItemKafkaEvent orderItemKafkaEvent, ItemProcessingStatus itemProcessingStatus);
}
