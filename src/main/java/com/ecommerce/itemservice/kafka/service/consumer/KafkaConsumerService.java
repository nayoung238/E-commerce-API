package com.ecommerce.itemservice.kafka.service.consumer;

import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.domain.item.service.ItemStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ItemStockService itemStockService;

    @KafkaListener(topics = {
            TopicConfig.REQUESTED_ORDER_TOPIC,
            TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC})
    public void updateStock(ConsumerRecord<String, OrderKafkaEvent> record) {
        if(record.value() != null) {
            log.info("Consuming message Success -> Topic: {}, OrderEventKey:{}",
                    record.topic(),
                    record.value().getOrderEventId());

            boolean isStreamsOnly = Objects.equals(record.topic(), TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC);
            itemStockService.updateStock(record.value(), isStreamsOnly);
        }
    }
}
