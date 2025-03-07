package com.ecommerce.apicomposer.mypage.dto.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class OrderUpdatedEventDeserializer implements Deserializer<OrderUpdatedEvent> {

	ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

	@Override
	public OrderUpdatedEvent deserialize(String topic, byte[] data) {
		OrderUpdatedEvent message = null;
		try {
			message = objectMapper.readValue(data, OrderUpdatedEvent.class);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		return message;
	}
}
