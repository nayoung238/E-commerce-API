package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.internalevent.ordercreation.OrderCreationInternalEvent;
import com.ecommerce.orderservice.internalevent.InternalEventStatus;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
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
import java.util.stream.Collectors;

@Entity
@Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private OrderStatus orderStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    /*
        KStream-KTable Join 방식에서 사용
        -> 클라이언트 요청 발생 즉시 DB insert하지 않고,
           Kafka 내부에서 최종 결과가 만들어지면 DB에 접근하므로
           지연 시간 체크를 위해 requestedAt 필드 추가
     */
    private LocalDateTime requestedAt;

    public static Order of(OrderRequestDto orderRequestDto) {
        List<OrderItem> orderItems = orderRequestDto.getOrderItemRequestDtos().stream()
                .map(OrderItem::of)
                .collect(Collectors.toList());

        return Order.builder()
                .accountId(orderRequestDto.getAccountId())
                .orderItems(orderItems)
                .orderStatus(OrderStatus.WAITING)
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
                .orderStatus(orderKafkaEvent.getOrderStatus())
                .createdAt(orderKafkaEvent.getCreatedAt())
                .requestedAt(orderKafkaEvent.getRequestedAt())
                .build();

        order.getOrderItems().
                forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
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

        HashMap<Long, OrderStatus> orderStatusHashMap = new HashMap<>();
        orderKafkaEvent.getOrderItemKafkaEvents()
                .forEach(o -> orderStatusHashMap.put(o.getItemId(), o.getOrderStatus()));

        this.orderItems
                .forEach(o -> o.updateOrderStatus(orderStatusHashMap.get(o.getItemId())));
    }

    public OrderCreationInternalEvent getOrderCreationInternalEvent() {
        return OrderCreationInternalEvent.builder()
                .orderEventId(orderEventId)
                .publicationStatus(InternalEventStatus.init)
                .build();
    }
}
