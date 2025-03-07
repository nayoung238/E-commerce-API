package com.ecommerce.apicomposer.mypage.dto.kafka;

import com.ecommerce.apicomposer.mypage.enums.OrderProcessingStatus;

public record OrderUpdatedEvent (

	long userId,
	String orderEventId,
	OrderProcessingStatus orderProcessingStatus
) { }
