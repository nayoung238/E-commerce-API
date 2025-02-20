package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;

    private Long quantity;

    @Enumerated(EnumType.STRING)
    private OrderProcessingStatus orderProcessingStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItem(Long id, Long itemId, Long quantity, OrderProcessingStatus orderProcessingStatus, Order order) {
        this.id = id;
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderProcessingStatus = orderProcessingStatus;
        this.order = order;
    }

    public static OrderItem of(OrderItemRequestDto orderItemRequestDto) {
        return OrderItem.builder()
                .itemId(orderItemRequestDto.itemId())
                .quantity(orderItemRequestDto.quantity())
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .build();
    }

    public static OrderItem of(OrderItemKafkaEvent orderItemEvent) {
        return OrderItem.builder()
                .itemId(orderItemEvent.getItemId())
                .quantity(orderItemEvent.getQuantity())
                .orderProcessingStatus(orderItemEvent.getOrderProcessingStatus())
                .build();
    }

    // Test 코드에서 사용
    public static OrderItem of(long itemId, long quantity, OrderProcessingStatus orderProcessingStatus) {
        return OrderItem.builder()
                .itemId(itemId)
                .quantity(3L)
                .orderProcessingStatus(orderProcessingStatus)
                .build();
    }

    public void initializeOrder(Order order) {
        this.order = order;
    }

    public void updateOrderStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }
}
