package com.nayoung.orderservice.messagequeue;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderRepository;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderRepository orderRepository;

    @KafkaListener(topics = "update-order-status-topic")
    public void updateOrderStatus(String kafkaMessage) {
        Map<Object, Object> map = new HashMap<>();
        ObjectMapper mapper = new ObjectMapper();
        try {
            map = mapper.readValue(kafkaMessage, new TypeReference<Map<Object, Object>>() {});
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Long id = Long.parseLong(String.valueOf(map.get("orderId")));
        Order order = orderRepository.findById(id).orElseThrow();

        boolean isAvailableStockUpdate = Boolean.parseBoolean(String.valueOf(map.get("available")));
        if(isAvailableStockUpdate) {
            order.updateOrderStatus(OrderStatus.SUCCESS);
        }
        else {
            order.updateOrderStatus(OrderStatus.FAILED);
        }
        orderRepository.save(order);
    }
}