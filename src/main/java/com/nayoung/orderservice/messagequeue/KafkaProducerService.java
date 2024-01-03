package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, OrderDto> kafkaTemplate;

    public void send(String kafkaTopic, OrderDto value) {
        sendMessage(kafkaTopic, null, value);
    }

    public void send(String kafkaTopic, String key, OrderDto value) {
        sendMessage(kafkaTopic, key, value);
    }

    private void sendMessage(String topic, String key, OrderDto value) {
        kafkaTemplate.send(topic, key, value)
                .addCallback(
                        result -> {  // SuccessCallback
                            assert result != null;
                            RecordMetadata metadata = result.getRecordMetadata();
                            log.info("Producing message Success -> topic: {}, partition: {}, offset: {}, event Id: {}",
                                    metadata.topic(),
                                    metadata.partition(),
                                    metadata.offset(),
                                    value.getEventId());},
                        exception -> log.error("Producing message Failure -> " + exception.getMessage()));  // FailureCallback
    }
}
