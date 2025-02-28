package com.ecommerce.couponservice.kafka.service;

import com.ecommerce.couponservice.kafka.dto.CouponUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

	private final KafkaTemplate<Object, CouponUpdatedEvent> couponUpdatedEventKafkaTemplate;

	public void send(String topic, @Payload(required = false) CouponUpdatedEvent value) {
		couponUpdatedEventKafkaTemplate.send(topic, value)
			.whenComplete((couponUpdatedEventSendResult, throwable) -> {
				if(throwable == null) {
					RecordMetadata metadata = couponUpdatedEventSendResult.getRecordMetadata();
					log.info("Kafka event published successfully -> topic: {}, userId: {}, couponName: {}",
						metadata.topic(),
						couponUpdatedEventSendResult.getProducerRecord().value().userId(),
						couponUpdatedEventSendResult.getProducerRecord().value().couponName());
				} else {
					// TODO: 카프카 이벤트 재발행
				}
			});
	}
}
