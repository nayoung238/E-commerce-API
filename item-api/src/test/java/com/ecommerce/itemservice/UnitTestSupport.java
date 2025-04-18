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

    protected OrderKafkaEvent getOrderKafkaEvent(long userId, List<Long> itemIds, long quantity, OrderStatus orderStatus) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = itemIds.stream()
                .map(i -> getOrderItemKafkaEvent(i, quantity, orderStatus))
                .toList();

        return OrderKafkaEvent.of(
                getOrderEventId(userId),
                userId,
			    orderStatus,
                orderItemKafkaEvents,
                LocalDateTime.now());
    }

    private String getOrderEventId(long userId) {
        return userId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /*
        OrderStatus 설정 이유: Order-Service에서 OrderStatus.WAITING 설정해서 이벤트 전송함
     */
    protected OrderItemKafkaEvent getOrderItemKafkaEvent(long itemId, long quantity, OrderStatus status) {
        return OrderItemKafkaEvent.of(itemId, quantity, status);
    }
}
