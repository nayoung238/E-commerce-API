package com.ecommerce.apicomposer.mypage.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class CouponUpdatedEventDeserializer implements Deserializer<CouponUpdatedEvent> {

	ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@Override
	public CouponUpdatedEvent deserialize(String topic, byte[] data) {
		CouponUpdatedEvent message = null;
		try {
			message = objectMapper.readValue(data, CouponUpdatedEvent.class);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return message;
	}
}
