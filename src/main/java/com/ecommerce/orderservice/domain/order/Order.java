package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.internalevent.ordercreation.OrderCreationInternalEvent;
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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "orders", indexes = @Index(name = "idx_order_event_id", columnList = "orderEventId"))
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // Kafka KTable & KStream key
    @Column(name = "order_event_id", unique = true)
    private String orderEventId;

    @Column(name = "account_id")
    private Long accountId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    private OrderProcessingStatus orderProcessingStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime requestedAt;

    @Builder(access = AccessLevel.PRIVATE)
    private Order(Long id, String orderEventId, Long accountId,
                  List<OrderItem> orderItems, OrderProcessingStatus orderProcessingStatus,
                  LocalDateTime requestedAt) {
        this.id = id;
        this.orderEventId = orderEventId;
        this.accountId = accountId;
        this.orderItems = orderItems;
        this.orderProcessingStatus = orderProcessingStatus;
        this.requestedAt = requestedAt;
    }

    public static Order of(OrderRequestDto orderRequestDto) {
        List<OrderItem> orderItems = orderRequestDto.getOrderItemRequestDtos().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        return Order.builder()
                .accountId(orderRequestDto.getAccountId())
                .orderItems(orderItems)
                .orderProcessingStatus(OrderProcessingStatus.PROCESSING)
                .requestedAt(LocalDateTime.now())
                .build();
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

    public OrderCreationInternalEvent getOrderCreationInternalEvent() {
        return OrderCreationInternalEvent.init(orderEventId);
    }
}
