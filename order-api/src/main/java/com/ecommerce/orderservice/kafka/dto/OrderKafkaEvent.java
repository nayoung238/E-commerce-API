package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.order.dto.request.OrderCreationRequest;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderKafkaEvent {

    private String orderEventId;
    private Long userId;
    private OrderStatus orderStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private LocalDateTime requestedAt;

    public static OrderKafkaEvent of(Order order) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = order
                .getOrderItems()
                .stream().map(OrderItemKafkaEvent::of)
                .collect(Collectors.toList());

        return OrderKafkaEvent.builder()
                .orderEventId(order.getOrderEventId())
                .userId(order.getUserId())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus() : null)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(OrderDetailResponse orderDetailResponse) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderDetailResponse
                .getOrderItems()
                .stream().map(OrderItemKafkaEvent::of)
                .collect(Collectors.toList());

        return OrderKafkaEvent.builder()
                .orderEventId(orderDetailResponse.getOrderEventId())
                .userId(orderDetailResponse.getUserId())
                .orderStatus(orderDetailResponse.getOrderStatus() != null ? orderDetailResponse.getOrderStatus() : null)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(OrderCreationRequest orderCreationRequest, String orderEventId) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderCreationRequest
                .orderItems()
                .stream()
                .map(OrderItemKafkaEvent::of)
                .toList();

        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .userId(orderCreationRequest.userId())
                .orderStatus(OrderStatus.PROCESSING)
                .orderItemKafkaEvents(orderItemKafkaEvents)
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

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
        this.orderItemKafkaEvents
                .forEach(orderItem -> orderItem.updateOrderStatus(orderStatus));
    }
}
