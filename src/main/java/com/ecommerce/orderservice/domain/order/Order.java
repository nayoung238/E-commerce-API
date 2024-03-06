package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.kafka.dto.OrderEvent;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders", indexes = @Index(name = "idx_order_event_key", columnList = "orderEventKey"))
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // Kafka KTable & KStream key
    @Column(name = "order_event_key", unique = true)
    private String orderEventKey;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime requestedAt;

    public static Order fromTemporaryOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromTemporaryOrderItemDto)
                .collect(Collectors.toList());

        return Order.builder()
                .userId(orderDto.getUserId())
                .orderItems(orderItems)
                .orderStatus(OrderStatus.WAITING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static Order of(OrderEvent orderEvent) {
        List<OrderItem> orderItems = orderEvent.getOrderItemEvents().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        Order order = Order.builder()
                .orderEventKey(orderEvent.getOrderEventKey())
                .userId(orderEvent.getUserId())
                .orderItems(orderItems)
                .orderStatus(orderEvent.getOrderStatus())
                .createdAt(orderEvent.getCreatedAt())
                .requestedAt(orderEvent.getRequestedAt())
                .build();

        order.getOrderItems().
                forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
    }

    public void initializeOrderEventKey(String orderEventKey) {
        this.orderEventKey = orderEventKey;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
        this.orderItems
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }

    public void updateOrderStatus(OrderEvent orderEvent) {
        this.orderStatus = orderEvent.getOrderStatus();

        HashMap<Long, OrderStatus> orderStatusHashMap = new HashMap<>();
        orderEvent.getOrderItemEvents()
                .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderStatus()));

        this.orderItems
                .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
    }
}
