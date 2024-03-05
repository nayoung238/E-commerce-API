package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderDto> orderDtoKafkaTemplate;

    public void setTombstoneRecord(String topic, String key) {
        assert key != null;
        send(topic, key, null);
    }

    public void send(String topic, String key, @Payload(required = false) OrderDto value) {
        orderDtoKafkaTemplate.send(topic, key, value)
                .whenComplete((stringOrderDtoSendResult, throwable) -> {
                    if(throwable == null) {
                        RecordMetadata metadata = stringOrderDtoSendResult.getRecordMetadata();
                        if(stringOrderDtoSendResult.getProducerRecord().value() != null) {
                            log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, event Id: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset(),
                                    stringOrderDtoSendResult.getProducerRecord().key());
                        } else {
                            log.info("Tombstone Record -> topic: {}, partition: {}, event Id: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    stringOrderDtoSendResult.getProducerRecord().key());
                        }
                    } else {
                        log.error("Producing message Failure -> " + throwable.getMessage());
                    }
                });
    }
}
