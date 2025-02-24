package com.ecommerce.itemservice.kafka.service;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.item.service.ItemStockService;
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
            log.info("Event consumed successfully -> Topic: {}, OrderEventKey: {}",
                    record.topic(),
                    record.value().getOrderEventId());

            boolean isStreamsOnly = Objects.equals(record.topic(), TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC);
            itemStockService.updateStock(record.value(), isStreamsOnly);
        }
    }

    @KafkaListener(topics = TopicConfig.ITEM_STOCK_AGGREGATION_RESULTS_TOPIC,
                    containerFactory = "kafkaStreamsListenerContainerFactory")
    public void updateStockAggregationResults(ConsumerRecord<String, Long> record) {
        log.info("Event consumed successfully -> Topic: {}, ItemId: {}, Quantity: {}",
                record.topic(),
                record.key(),
                record.value());

        if(record.value() != null && record.value() != 0) {
            ItemProcessingStatus itemUpdateStatus = (record.value() > 0) ?
                    ItemProcessingStatus.STOCK_PRODUCTION : ItemProcessingStatus.STOCK_CONSUMPTION;
            itemStockService.updateStockWithPessimisticLock(Long.valueOf(record.key()), record.value(), itemUpdateStatus);
        }
    }
}
