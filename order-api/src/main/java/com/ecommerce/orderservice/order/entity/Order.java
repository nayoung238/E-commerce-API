package com.ecommerce.orderservice.order.entity;

import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.dto.request.OrderCreationRequest;
import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Entity
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = @Index(name = "idx_order_event_id", columnList = "orderEventId"))
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // Kafka KTable & KStream key
    @Column(name = "order_event_id", unique = true, nullable = false)
    private String orderEventId;

    @Column(nullable = false)
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    public static Order of(OrderCreationRequest orderCreationRequest) {
        List<OrderItem> orderItems = orderCreationRequest.orderItems().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        Order order = Order.builder()
                .userId(orderCreationRequest.userId())
                .orderItems(orderItems)
                .orderStatus(OrderStatus.PROCESSING)
                .requestedAt(LocalDateTime.now())
                .build();

        order.getOrderItems()
            .forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
    }

    public static Order of(OrderKafkaEvent orderKafkaEvent) {
        List<OrderItem> orderItems = orderKafkaEvent.getOrderItemKafkaEvents().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        Order order = Order.builder()
                .orderEventId(orderKafkaEvent.getOrderEventId())
                .userId(orderKafkaEvent.getUserId())
                .orderItems(orderItems)
                .orderStatus(orderKafkaEvent.getOrderStatus())
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();

        order.getOrderItems().
                forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
    }

    // Test 코드에서 사용
    public static Order of(String orderEventId, long userId,
                           List<OrderItem> orderItems,
                           OrderStatus orderStatus,
                           LocalDateTime requestedAt) {
        return Order.builder()
                .orderEventId(orderEventId)
                .userId(userId)
                .orderItems(orderItems)
                .orderStatus(orderStatus)
                .requestedAt(requestedAt)
                .build();
    }

    public void initializeOrderEventId(String orderEventId) {
        this.orderEventId = orderEventId;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
        this.orderItems
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }

    public void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        this.orderStatus = orderKafkaEvent.getOrderStatus();

        Map<Long, OrderStatus> orderStatusMap = orderKafkaEvent.getOrderItemKafkaEvents()
            .stream()
            .collect(Collectors.toMap(OrderItemKafkaEvent::getItemId, OrderItemKafkaEvent::getOrderStatus));

        this.orderItems.forEach(oi ->
            oi.updateOrderStatus(orderStatusMap.getOrDefault(oi.getItemId(), this.orderStatus))
        );
    }

    public OrderInternalEvent getOrderInternalEvent() {
        return OrderInternalEvent.of(userId, orderEventId, OrderStatus.CREATION);
    }
}
