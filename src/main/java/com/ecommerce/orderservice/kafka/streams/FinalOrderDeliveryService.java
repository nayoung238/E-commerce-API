package com.ecommerce.orderservice.kafka.streams;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.streams.kstream.KStream;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FinalOrderDeliveryService {

    @Autowired
    public void sendToKafkaTopic(@Qualifier("createOrder") KStream<String, OrderDto> finalOrder) {
        finalOrder.to(KStreamKTableJoinConfig.FINAL_ORDER_CREATION_TOPIC);
    }
}