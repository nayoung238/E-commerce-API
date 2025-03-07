package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
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
    private OrderProcessingStatus orderProcessingStatus;
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
                .orderProcessingStatus(order.getOrderProcessingStatus() != null ? order.getOrderProcessingStatus() : null)
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
                .orderProcessingStatus(orderDetailResponse.getOrderProcessingStatus() != null ? orderDetailResponse.getOrderProcessingStatus() : null)
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
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(String orderEventId, OrderProcessingStatus orderProcessingStatus) {
        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .orderProcessingStatus(orderProcessingStatus)
                .build();
    }

    public void updateOrderStatus(OrderKafkaEvent orderEvent) {
        if(orderEvent.getOrderItemKafkaEvents() != null) {
            this.orderProcessingStatus = orderEvent.getOrderProcessingStatus();

            HashMap<Long, OrderProcessingStatus> orderStatusHashMap = new HashMap<>();
            orderEvent.getOrderItemKafkaEvents()
                    .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderProcessingStatus()));

            this.orderItemKafkaEvents
                    .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
        }
        else updateOrderStatus(orderEvent.getOrderProcessingStatus());
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemKafkaEvents
                .forEach(orderItem -> orderItem.updateOrderStatus(orderProcessingStatus));
    }
}
