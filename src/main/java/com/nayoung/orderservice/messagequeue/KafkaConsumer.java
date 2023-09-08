package com.nayoung.orderservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderService orderService;

    @KafkaListener(topics = "e-commerce.order.local.order-details")
    public void updateOrderStatus(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            Map<Object, Object> map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
            Long orderId = Long.parseLong(String.valueOf(map.get("orderId")));
            Long customerAccountId = Long.parseLong(String.valueOf(map.get("customerAccountId")));

            boolean isAvailableStockUpdate = Boolean.parseBoolean(String.valueOf(map.get("isAvailableToOrder")));
            if(isAvailableStockUpdate)
                orderService.updateOrderStatus(OrderStatus.SUCCEED, orderId);
            else
                orderService.updateOrderStatus(OrderStatus.FAILED, orderId);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }
}