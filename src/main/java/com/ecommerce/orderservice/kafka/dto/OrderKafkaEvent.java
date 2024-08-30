package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderKafkaEvent {

    private String orderEventId;
    private OrderStatus orderStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private Long accountId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public static OrderKafkaEvent of(Order order) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = order
                .getOrderItems()
                .stream().map(OrderItemKafkaEvent::of)
                .collect(Collectors.toList());

        return OrderKafkaEvent.builder()
                .orderEventId(order.getOrderEventId())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus() : null)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .accountId(order.getAccountId())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : null)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(OrderDto orderDto) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderDto
                .getOrderItemDtos()
                .stream().map(OrderItemKafkaEvent::of)
                .collect(Collectors.toList());

        return OrderKafkaEvent.builder()
                .orderEventId(orderDto.getOrderEventId())
                .orderStatus(orderDto.getOrderStatus() != null ? orderDto.getOrderStatus() : null)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .accountId(orderDto.getAccountId())
                .createdAt(orderDto.getCreatedAt() != null ? orderDto.getCreatedAt() : null)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(OrderRequestDto orderRequestDto, String orderEventId) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderRequestDto
                .getOrderItemRequestDtos()
                .stream()
                .map(OrderItemKafkaEvent::of)
                .toList();

        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .orderStatus(OrderStatus.WAITING)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .accountId(orderRequestDto.getAccountId())
                .createdAt(null)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(String orderEventId, OrderStatus orderStatus) {
        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .orderStatus(orderStatus)
                .build();
    }

    public void updateOrderStatus(OrderKafkaEvent orderEvent) {
        if(orderEvent.getOrderItemKafkaEvents() != null) {
            this.orderStatus = orderEvent.getOrderStatus();

            HashMap<Long, OrderStatus> orderStatusHashMap = new HashMap<>();
            orderEvent.getOrderItemKafkaEvents()
                    .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderStatus()));

            this.orderItemKafkaEvents
                    .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
        }
        else updateOrderStatus(orderEvent.getOrderStatus());
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
        this.orderItemKafkaEvents
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }
}
