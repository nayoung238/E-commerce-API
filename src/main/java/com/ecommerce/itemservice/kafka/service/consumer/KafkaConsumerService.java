package com.ecommerce.itemservice.kafka.service.consumer;

import com.ecommerce.itemservice.kafka.dto.OrderDto;
import com.ecommerce.itemservice.domain.item.service.ItemStockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import static com.ecommerce.itemservice.kafka.config.consumer.KafkaConsumerConfig.REQUESTED_ORDER_TOPIC;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ItemStockService itemStockService;

    @KafkaListener(topics = REQUESTED_ORDER_TOPIC)
    public void updateStock(ConsumerRecord<String, OrderDto> record) {
        if(record.value() != null) {
            log.info("Consuming message Success -> Topic: {}, Event Id:{}",
                    record.topic(),
                    record.value().getEventId());

           itemStockService.updateStock(record.value());
        }
    }
}
