package com.ecommerce.apicomposer.common.config;

import com.ecommerce.apicomposer.mypage.dto.kafka.CouponUpdatedEvent;
import com.ecommerce.apicomposer.mypage.dto.kafka.CouponUpdatedEventDeserializer;
import com.ecommerce.apicomposer.mypage.dto.kafka.OrderUpdatedEvent;
import com.ecommerce.apicomposer.mypage.dto.kafka.OrderUpdatedEventDeserializer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import java.util.HashMap;
import java.util.Map;

@EnableKafka
@Configuration
public class KafkaConsumerConfig {

	@Value("${spring.kafka.bootstrap-servers}")
	private String BOOTSTRAP_SERVER;

	@Value("${spring.kafka.consumer.group-id}")
	private String CONSUMER_GROUP_ID;

	@Bean
	public ConsumerFactory<String, OrderUpdatedEvent> orderUpdatedEventConsumerFactory() {
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, OrderUpdatedEventDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(properties);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> kafkaOrderListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, OrderUpdatedEvent> kafkaListenerContainerFactory
			= new ConcurrentKafkaListenerContainerFactory<>();

		kafkaListenerContainerFactory.setConsumerFactory(orderUpdatedEventConsumerFactory());
		return kafkaListenerContainerFactory;
	}

	@Bean
	public ConsumerFactory<Object, CouponUpdatedEvent> couponUpdatedEventConsumerFactory() {
		Map<String, Object> properties = new HashMap<>();
		properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVER);
		properties.put(ConsumerConfig.GROUP_ID_CONFIG, CONSUMER_GROUP_ID);
		properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, CouponUpdatedEventDeserializer.class);
		return new DefaultKafkaConsumerFactory<>(properties);
	}

	@Bean
	public ConcurrentKafkaListenerContainerFactory<String, CouponUpdatedEvent> kafkaCouponListenerContainerFactory() {
		ConcurrentKafkaListenerContainerFactory<String, CouponUpdatedEvent> kafkaListenerContainerFactory
			= new ConcurrentKafkaListenerContainerFactory<>();

		kafkaListenerContainerFactory.setConsumerFactory(couponUpdatedEventConsumerFactory());
		return kafkaListenerContainerFactory;
	}
}
