package com.ecommerce.accountservice.kafka.dto;

import com.fasterxml.jackson.core.json.JsonReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
public class CouponIssuanceResultKafkaEventDeserializer implements Deserializer<CouponIssuanceResultKafkaEvent> {

    ObjectMapper objectMapper;

    public CouponIssuanceResultKafkaEventDeserializer() {
        this.objectMapper = JsonMapper.builder()
                .addModule(new JavaTimeModule())
                .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
                .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }

    @Override
    public CouponIssuanceResultKafkaEvent deserialize(String topic, byte[] data) {
        if(data == null || data.length == 0) {
            return null;
        }

        try {
            String cleanJson = new String(data, StandardCharsets.UTF_8)
                    .replaceAll("\\p{Cc}", "")
                    .trim();

            if(cleanJson.isEmpty()) return null;

            return objectMapper.readValue(cleanJson, CouponIssuanceResultKafkaEvent.class);
        } catch (IOException e) {
            log.error(e.getMessage());
            return null;
        }
    }
}
