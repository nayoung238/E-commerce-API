package com.ecommerce.orderservice.kafka.service.streams;

import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinalOrderDeliveryService {

    @Autowired
    public void sendToKafkaTopic(@Qualifier("createOrder") KStream<String, OrderKafkaEvent> finalOrder) {
        finalOrder.to(TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC);
    }
}