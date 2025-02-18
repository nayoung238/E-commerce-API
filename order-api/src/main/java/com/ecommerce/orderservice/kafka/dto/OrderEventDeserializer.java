package com.ecommerce.orderservice.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class OrderEventDeserializer implements Deserializer<OrderKafkaEvent> {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public OrderKafkaEvent deserialize(String topic, byte[] data) {
        OrderKafkaEvent message = null;
        try {
            message = objectMapper.readValue(data, OrderKafkaEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return message;
    }
}
