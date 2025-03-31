package com.ecommerce.apicomposer.mypage.dto.kafka;

import com.ecommerce.apicomposer.mypage.enums.OrderStatus;

public record OrderUpdatedEvent (

	long userId,
	String orderEventId,
	OrderStatus orderStatus
) { }
