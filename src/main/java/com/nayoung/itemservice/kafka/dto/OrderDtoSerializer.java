package com.nayoung.itemservice.kafka.dto;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class OrderDtoSerializer implements Serializer<OrderDto> {

    ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Override
    public byte[] serialize(String topic, OrderDto data) {
        byte[] serializeOrder = null;
        try {
            serializeOrder = objectMapper.writeValueAsBytes(data);
        } catch(JsonProcessingException e) {
            log.error("Json processing exception: " + e.getMessage());
        }
        return serializeOrder;
    }
}
