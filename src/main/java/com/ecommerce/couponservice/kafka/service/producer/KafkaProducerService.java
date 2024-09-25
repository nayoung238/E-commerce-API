package com.ecommerce.couponservice.kafka.service.producer;

import com.ecommerce.couponservice.internalevent.InternalEventStatus;
import com.ecommerce.couponservice.internalevent.service.InternalEventService;
import com.ecommerce.couponservice.kafka.dto.CouponIssuanceResultKafkaEvent;
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

    private final KafkaTemplate<String, CouponIssuanceResultKafkaEvent> couponIssuanceResultDtoKafkaTemplate;
    private final InternalEventService internalEventService;

    public void send(String topic, String key, @Payload(required = false) CouponIssuanceResultKafkaEvent value) {
        couponIssuanceResultDtoKafkaTemplate.send(topic, key, value)
                .whenComplete((result, throwable) -> {
                    if(throwable == null) {
                        internalEventService.updatePublicationStatus(value.getCouponId(), value.getAccountId(), InternalEventStatus.send_success);

                        RecordMetadata metadata = result.getRecordMetadata();
                        log.info("Kafka event published successfully -> topic: {}, partition: {}, offset: {}, orderEventKey: {}",
                                metadata.topic(),
                                metadata.partition(),
                                metadata.offset(),
                                result.getProducerRecord().key());
                    } else {
                        internalEventService.updatePublicationStatus(value.getCouponId(), value.getAccountId(), InternalEventStatus.send_fail);
                        log.error("Failed to publish Kafka event: " + throwable.getMessage());
                    }
                });
    }
}
