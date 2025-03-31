package com.ecommerce.orderservice.order.entity;

import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.dto.request.OrderItemRequest;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private Long quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public static OrderItem of(OrderItemRequest orderItemRequest) {
        return OrderItem.builder()
                .itemId(orderItemRequest.itemId())
                .quantity(orderItemRequest.quantity())
                .orderStatus(OrderStatus.PROCESSING)
                .build();
    }

    public static OrderItem of(OrderItemKafkaEvent orderItemEvent) {
        return OrderItem.builder()
                .itemId(orderItemEvent.getItemId())
                .quantity(orderItemEvent.getQuantity())
                .orderStatus(orderItemEvent.getOrderStatus())
                .build();
    }

    // Test 코드에서 사용
    public static OrderItem of(long itemId, long quantity, OrderStatus orderStatus) {
        return OrderItem.builder()
                .itemId(itemId)
                .quantity(3L)
                .orderStatus(orderStatus)
                .build();
    }

    public void initializeOrder(Order order) {
        this.order = order;
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
