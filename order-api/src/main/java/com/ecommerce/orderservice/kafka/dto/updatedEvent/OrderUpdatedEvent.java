package com.ecommerce.orderservice.kafka.dto.updatedEvent;

import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import lombok.Builder;

@Builder
public record OrderUpdatedEvent (

	long userId,
	String orderEventId,
	OrderProcessingStatus orderProcessingStatus
) {

	public static OrderUpdatedEvent of(OrderInternalEvent orderInternalEvent) {
		return OrderUpdatedEvent.builder()
			.userId(orderInternalEvent.getUserId())
			.orderEventId(orderInternalEvent.getOrderEventId())
			.orderProcessingStatus(orderInternalEvent.getOrderProcessingStatus())
			.build();
	}
}
