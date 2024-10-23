package com.ecommerce.itemservice;

import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@ActiveProfiles("test")
public class UnitTestSupport {

    protected OrderKafkaEvent getOrderKafkaEvent(long accountId, List<Long> itemIds, long quantity, OrderProcessingStatus orderProcessingStatus) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = itemIds.stream()
                .map(i -> getOrderItemKafkaEvent(i, quantity, orderProcessingStatus))
                .toList();

        return OrderKafkaEvent.of(
                getOrderEventId(accountId),
                accountId,
                orderProcessingStatus,
                orderItemKafkaEvents,
                LocalDateTime.now());
    }

    private String getOrderEventId(long accountId) {
        return accountId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    /*
        OrderStatus 설정 이유: Order-Service에서 OrderStatus.WAITING 설정해서 이벤트 전송함
     */
    protected OrderItemKafkaEvent getOrderItemKafkaEvent(long itemId, long quantity, OrderProcessingStatus status) {
        return OrderItemKafkaEvent.of(itemId, quantity, status);
    }
}
