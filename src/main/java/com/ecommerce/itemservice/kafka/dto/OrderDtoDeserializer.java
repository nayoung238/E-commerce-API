package com.ecommerce.itemservice.kafka.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class OrderDtoDeserializer implements Deserializer<OrderDto> {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    @Override
    public OrderDto deserialize(String topic, byte[] data) {
        OrderDto orderDetails = null;
        try {
            orderDetails = objectMapper.readValue(data, OrderDto.class);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
        return orderDetails;
    }
}
