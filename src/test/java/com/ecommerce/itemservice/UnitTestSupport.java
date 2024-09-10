package com.ecommerce.itemservice;

import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
public class UnitTestSupport {

    protected OrderKafkaEvent getOrderKafkaEvent(long accountId, List<Long> itemIds, long quantity, OrderStatus status) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = itemIds.stream()
                .map(i -> getOrderItemKafkaEvent(i, quantity, status))
                .toList();

        return OrderKafkaEvent.builder()
                .orderEventId(getOrderEventId(accountId))
                .orderStatus(status)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    private String getOrderEventId(long accountId) {
        return accountId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /*
        OrderStatus 설정 이유: Order-Service에서 OrderStatus.WAITING 설정해서 이벤트 전송함
     */
    protected OrderItemKafkaEvent getOrderItemKafkaEvent(long itemId, long quantity, OrderStatus status) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderStatus(status)
                .build();
    }
}
