package com.ecommerce.orderservice.kafka.dto;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
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
public class OrderEvent {

    private String orderEventKey;
    private OrderStatus orderStatus;
    private List<OrderItemEvent> orderItemEvents;
    private Long userId;
    private LocalDateTime createdAt;
    private LocalDateTime requestedAt;

    public static OrderEvent of(Order order) {
        List<OrderItemEvent> orderItemEvents = order
                .getOrderItems()
                .stream().map(OrderItemEvent::of)
                .collect(Collectors.toList());

        return OrderEvent.builder()
                .orderEventKey(order.getOrderEventKey())
                .orderStatus(order.getOrderStatus() != null ? order.getOrderStatus() : null)
                .orderItemEvents(orderItemEvents)
                .userId(order.getUserId())
                .createdAt(order.getCreatedAt() != null ? order.getCreatedAt() : null)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderEvent of(OrderDto orderDto) {
        List<OrderItemEvent> orderItemEvents = orderDto
                .getOrderItemDtos()
                .stream().map(OrderItemEvent::of)
                .collect(Collectors.toList());

        return OrderEvent.builder()
                .orderEventKey(orderDto.getOrderEventKey())
                .orderStatus(orderDto.getOrderStatus() != null ? orderDto.getOrderStatus() : null)
                .orderItemEvents(orderItemEvents)
                .userId(orderDto.getUserId())
                .createdAt(orderDto.getCreatedAt() != null ? orderDto.getCreatedAt() : null)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static OrderEvent of(String orderEventKey, OrderStatus orderStatus) {
        return OrderEvent.builder()
                .orderEventKey(orderEventKey)
                .orderStatus(orderStatus)
                .build();
    }

    public void updateOrderStatus(OrderEvent orderEvent) {
        if(orderEvent.getOrderItemEvents() != null) {
            this.orderStatus = orderEvent.getOrderStatus();

            HashMap<Long, OrderStatus> orderStatusHashMap = new HashMap<>();
            orderEvent.getOrderItemEvents()
                    .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderStatus()));

            this.orderItemEvents
                    .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
        }
        else updateOrderStatus(orderEvent.getOrderStatus());
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
        this.orderItemEvents
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }
}
