package com.nayoung.itemservice.kafka.producer;

import com.nayoung.itemservice.kafka.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducer {

    private final KafkaTemplate<String, Long> stockQuantityTemplate;
    private final KafkaTemplate<String, OrderDto> orderDtoKafkaTemplate;

    public void sendMessage(String topic, String key, Long value) {
        stockQuantityTemplate.send(topic, key, value)
                .whenComplete((stringLongSendResult, throwable) -> {
                    if(throwable == null) {
                        RecordMetadata metadata = stringLongSendResult.getRecordMetadata();
                        log.info("Producing message Success -> topic: {}, partition: {}, offset: {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset());
                    } else {
                        log.error("Producing message Failure " + throwable.getMessage());
                    }
                });
    }

    public void sendMessage(String topic, String key, OrderDto value) {
        orderDtoKafkaTemplate.send(topic, key, value)
                .whenComplete((stringOrderDtoSendResult, throwable) -> {
                    if(throwable == null) {
                        RecordMetadata metadata = stringOrderDtoSendResult.getRecordMetadata();
                        log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, event Id: {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset(),
                                value.getEventId());
                    } else {
                        log.error("Producing message Failure " + throwable.getMessage());
                    }
                });
    }
}
