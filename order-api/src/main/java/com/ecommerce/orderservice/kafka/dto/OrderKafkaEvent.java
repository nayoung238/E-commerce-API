package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderDto;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderKafkaEvent {

    private String orderEventId;
    private Long userId;
    private OrderProcessingStatus orderProcessingStatus;
    private List<OrderItemKafkaEvent> orderItemKafkaEvents;
    private LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderKafkaEvent(String orderEventId, long userId,
                            OrderProcessingStatus orderProcessingStatus, List<OrderItemKafkaEvent> orderItemKafkaEvents,
                            LocalDateTime requestedAt) {
        this.orderEventId = orderEventId;
        this.userId = userId;
        this.orderProcessingStatus = orderProcessingStatus;
        this.orderItemKafkaEvents = orderItemKafkaEvents;
        this.requestedAt = requestedAt;
    }

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

    public static OrderKafkaEvent of(OrderDto orderDto) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderDto
                .getOrderItemDtos()
                .stream().map(OrderItemKafkaEvent::of)
                .collect(Collectors.toList());

        return OrderKafkaEvent.builder()
                .orderEventId(orderDto.getOrderEventId())
                .userId(orderDto.getUserId())
                .orderProcessingStatus(orderDto.getOrderProcessingStatus() != null ? orderDto.getOrderProcessingStatus() : null)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderKafkaEvent of(OrderRequestDto orderRequestDto, String orderEventId) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = orderRequestDto
                .orderItemRequestDtos()
                .stream()
                .map(OrderItemKafkaEvent::of)
                .toList();

        return OrderKafkaEvent.builder()
                .orderEventId(orderEventId)
                .userId(orderRequestDto.userId())
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
