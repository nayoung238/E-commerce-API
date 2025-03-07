package com.ecommerce.orderservice;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderDto;
import com.ecommerce.orderservice.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
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

    protected OrderRequestDto getOrderRequestDto(Long userId, List<Long> orderItemIds) {
        List<OrderItemRequestDto> orderItemRequestDtos = orderItemIds.stream()
                .map(i -> OrderItemRequestDto.builder()
                    .itemId(i)
                    .quantity(3L)
                    .build())
                .toList();

        return OrderRequestDto.of(userId, orderItemRequestDtos);
    }

    protected Order getOrder(Long userId, List<Long> orderItemIds, OrderProcessingStatus orderProcessingStatus) {
        List<OrderItem> orderItems = orderItemIds.stream()
            .map(id -> OrderItem.builder()
                .itemId(id)
                .quantity(3L)
                .orderProcessingStatus(orderProcessingStatus)
                .build())
            .toList();

        Order order = Order.builder()
            .userId(userId)
            .orderEventId(getOrderEventId(userId))
            .orderItems(orderItems)
            .orderProcessingStatus(orderProcessingStatus)
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

    protected OrderKafkaEvent getOrderKafkaEvent(OrderDto orderDto, OrderProcessingStatus finalOrderProcessingStatus) {
        orderDto.updateOrderStatus(finalOrderProcessingStatus);
        orderDto.getOrderItemDtos()
                .forEach(o -> o.updateStatus(finalOrderProcessingStatus));
        return OrderKafkaEvent.of(orderDto);
    }
}
