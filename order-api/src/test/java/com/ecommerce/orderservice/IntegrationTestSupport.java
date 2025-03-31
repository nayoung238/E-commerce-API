package com.ecommerce.orderservice;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.order.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.order.dto.request.OrderCreationRequest;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;

@EmbeddedKafka(
        partitions = 2,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092"
        },
        ports = {9092})
@ActiveProfiles("test")
public class IntegrationTestSupport {

    protected OrderCreationRequest getOrderRequestDto(Long userId, List<Long> orderItemIds) {
        List<OrderItemRequest> orderItemRequests = orderItemIds.stream()
                .map(i -> OrderItemRequest.builder()
                    .itemId(i)
                    .quantity(3L)
                    .build())
                .toList();

        return OrderCreationRequest.of(userId, orderItemRequests);
    }

    protected Order getOrder(Long userId, List<Long> orderItemIds, OrderStatus orderStatus) {
        List<OrderItem> orderItems = orderItemIds.stream()
            .map(id -> OrderItem.builder()
                .itemId(id)
                .quantity(3L)
                .orderStatus(orderStatus)
                .build())
            .toList();

        Order order = Order.builder()
            .userId(userId)
            .orderEventId(getOrderEventId(userId))
            .orderItems(orderItems)
            .orderStatus(orderStatus)
            .build();

        orderItems.forEach(i -> i.initializeOrder(order));
        return order;
    }

    private String getOrderEventId(Long userId) {
        String[] uuid = UUID.randomUUID().toString().split("-");
        if (userId == null) {
            return uuid[0];
        }
        return userId + "-" + uuid[0];
    }

    protected OrderKafkaEvent getOrderKafkaEvent(OrderDetailResponse orderDetailResponse, OrderStatus finalOrderStatus) {
        orderDetailResponse.updateOrderStatus(finalOrderStatus);
        orderDetailResponse.getOrderItems()
                .forEach(o -> o.updateStatus(finalOrderStatus));
        return OrderKafkaEvent.of(orderDetailResponse);
    }
}
