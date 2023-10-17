package com.nayoung.itemservice.domain.item;

import com.nayoung.itemservice.messagequeue.client.OrderItemDto;

public interface StockUpdate {

    OrderItemDto updateStock(OrderItemDto orderItemDto, Long orderId, String eventId);
}
