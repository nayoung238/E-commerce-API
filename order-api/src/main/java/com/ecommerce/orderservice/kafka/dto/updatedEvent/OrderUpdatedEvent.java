package com.ecommerce.orderservice.kafka.dto.updatedEvent;

import com.ecommerce.orderservice.internalevent.order.event.OrderInternalEvent;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import lombok.Builder;

@Builder
public record OrderUpdatedEvent (

	long accountId,
	String orderEventId,
	OrderProcessingStatus orderProcessingStatus
) {

	public static OrderUpdatedEvent of(OrderInternalEvent orderInternalEvent) {
		return OrderUpdatedEvent.builder()
			.accountId(orderInternalEvent.getAccountId())
			.orderEventId(orderInternalEvent.getOrderEventId())
			.orderProcessingStatus(orderInternalEvent.getOrderProcessingStatus())
			.build();
	}
}
