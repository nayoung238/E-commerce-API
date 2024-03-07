package com.ecommerce.orderservice.kafka.streams;

import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderEvent;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinalOrderDeliveryService {

    @Autowired
    public void sendToKafkaTopic(@Qualifier("createOrder") KStream<String, OrderEvent> finalOrder) {
        finalOrder.to(TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC);
    }
}