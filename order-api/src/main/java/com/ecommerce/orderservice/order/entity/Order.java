package com.ecommerce.orderservice.order.entity;

import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.internalevent.order.event.OrderInternalEvent;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private Long accountId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderProcessingStatus orderProcessingStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    public static Order of(OrderRequestDto orderRequestDto) {
        List<OrderItem> orderItems = orderRequestDto.orderItemRequestDtos().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        Order order = Order.builder()
                .accountId(orderRequestDto.accountId())
                .orderItems(orderItems)
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
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
                .accountId(orderKafkaEvent.getAccountId())
                .orderItems(orderItems)
                .orderProcessingStatus(orderKafkaEvent.getOrderProcessingStatus())
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();

        order.getOrderItems().
                forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
    }

    // Test 코드에서 사용
    public static Order of(String orderEventId, long accountId,
                           List<OrderItem> orderItems,
                           OrderProcessingStatus orderProcessingStatus,
                           LocalDateTime requestedAt) {
        return Order.builder()
                .orderEventId(orderEventId)
                .accountId(accountId)
                .orderItems(orderItems)
                .orderProcessingStatus(orderProcessingStatus)
                .requestedAt(requestedAt)
                .build();
    }

    public void initializeOrderEventId(String orderEventId) {
        this.orderEventId = orderEventId;
    }

    public void updateOrderStatus(OrderProcessingStatus status) {
        this.orderProcessingStatus = status;
        this.orderItems
                .forEach(orderItem -> orderItem.updateOrderStatus(status));
    }

    public void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        this.orderProcessingStatus = orderKafkaEvent.getOrderProcessingStatus();

        HashMap<Long, OrderProcessingStatus> orderStatusHashMap = new HashMap<>();
        orderKafkaEvent.getOrderItemKafkaEvents()
                .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderProcessingStatus()));

        this.orderItems
                .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
    }

    public OrderInternalEvent getOrderInternalEvent() {
        return OrderInternalEvent.of(accountId, orderEventId, OrderProcessingStatus.CREATION);
    }
}
