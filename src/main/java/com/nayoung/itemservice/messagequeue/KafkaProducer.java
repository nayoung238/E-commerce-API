package com.nayoung.itemservice.messagequeue;

import com.nayoung.itemservice.messagequeue.client.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaProducerException;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Long> stockQuantityTemplate;
    private final KafkaTemplate<String, OrderDto> orderDtoKafkaTemplate;

    public void sendMessage(String topic, String key, Long value) {
        try {
            stockQuantityTemplate.send(topic, key, value)
                    .addCallback(result -> {
                        assert result != null;
                        RecordMetadata metadata = result.getRecordMetadata();
                        log.info("Producing message Success -> topic: {}, partition: {}, offset: {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset());
                    }, exception -> log.error("Producing message Failure " + exception.getMessage()));
        } catch (KafkaProducerException e) {
            throw e;
        }
    }

    public void sendMessage(String topic, String key, OrderDto value) {
        try {
            orderDtoKafkaTemplate.send(topic, key, value)
                    .addCallback(result -> {
                        assert result != null;
                        RecordMetadata metadata = result.getRecordMetadata();
                        log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, event Id: {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset(),
                                value.getEventId());
                    }, exception -> log.error("Producing message Failure " + exception.getMessage()));
        } catch (KafkaProducerException e) {
            throw e;
        }
    }
}
