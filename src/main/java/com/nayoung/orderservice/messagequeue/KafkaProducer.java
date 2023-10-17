package com.nayoung.orderservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    public void send(String kafkaTopic, String key, OrderDto value) {
        try {
            sendMessage(kafkaTopic, key, value);
        } catch(KafkaProducerException e) {
            log.error("Kafka Exception " + e.getMessage());
        }
    }

    private void sendMessage(String topic, String key, OrderDto value) {
        kafkaTemplate.send(topic, key, value)
                .addCallback(result -> {
                    assert result != null;
                    RecordMetadata metadata = result.getRecordMetadata();

                    log.info("Producing message Success -> topic: {}, partition: {}, offset: {}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                    }, exception -> log.error("Producing message Failure " + exception.getMessage()));
    }

    private String getMessage(OrderDto orderDto) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(orderDto);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonInString;
    }
}
