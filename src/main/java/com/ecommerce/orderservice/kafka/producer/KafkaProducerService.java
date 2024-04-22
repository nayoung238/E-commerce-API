package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderKafkaEvent> orderEventKafkaTemplate;

    public void setTombstoneRecord(String topic, String key) {
        assert key != null;
        send(topic, key, null);
    }

    public void send(String topic, String key, @Payload(required = false) OrderKafkaEvent value) {
        orderEventKafkaTemplate.send(topic, key, value)
                .whenComplete((stringOrderEventSendResult, throwable) -> {
                    if(throwable == null) {
                        RecordMetadata metadata = stringOrderEventSendResult.getRecordMetadata();
                        if(stringOrderEventSendResult.getProducerRecord().value() != null) {
                            log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, orderEventKey: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset(),
                                    stringOrderEventSendResult.getProducerRecord().key());
                        } else {
                            log.info("Tombstone Record -> topic: {}, partition: {}, orderEventKey: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    stringOrderEventSendResult.getProducerRecord().key());
                        }
                    } else {
                        log.error("Producing message Failure -> " + throwable.getMessage());
                    }
                });
    }
}
