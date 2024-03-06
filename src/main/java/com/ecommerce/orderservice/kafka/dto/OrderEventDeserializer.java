package com.ecommerce.orderservice.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class OrderEventDeserializer implements Deserializer<OrderEvent> {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public OrderEvent deserialize(String topic, byte[] data) {
        OrderEvent message = null;
        try {
            message = objectMapper.readValue(data, OrderEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return message;
    }
}
