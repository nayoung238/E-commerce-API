package com.ecommerce.apicomposer.common.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

	public static final String ORDER_UPDATED_TOPIC = "e-commerce.order.updated-order";
	public static final String COUPON_UPDATED_TOPIC = "e-commerce.coupon.updated-coupon";
}
