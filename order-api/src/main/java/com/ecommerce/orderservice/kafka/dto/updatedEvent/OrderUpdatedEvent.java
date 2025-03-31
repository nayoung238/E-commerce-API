package com.ecommerce.orderservice.kafka.dto.updatedEvent;

import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import lombok.Builder;

@Builder
public record OrderUpdatedEvent (

	long userId,
	String orderEventId,
	OrderStatus orderStatus
) {

	public static OrderUpdatedEvent of(OrderInternalEvent orderInternalEvent) {
		return OrderUpdatedEvent.builder()
			.userId(orderInternalEvent.getUserId())
			.orderEventId(orderInternalEvent.getOrderEventId())
			.orderStatus(orderInternalEvent.getOrderStatus())
			.build();
	}
}
