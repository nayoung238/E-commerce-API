package com.ecommerce.orderservice.kafka.service.producer;

import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderKafkaEvent> orderEventKafkaTemplate;
    private final InternalEventService internalEventService;

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
                            internalEventService.updatePublicationStatus(value.getOrderEventId(), InternalEventStatus.send_success);
                            log.info("Kafka event published successfully -> topic: {}, partition: {}, offset: {}, orderEventKey: {}",
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
                        internalEventService.updatePublicationStatus(value.getOrderEventId(), InternalEventStatus.send_fail);
                        log.error("Failed to publish Kafka event: " + throwable.getMessage());
                    }
                });
    }
}
