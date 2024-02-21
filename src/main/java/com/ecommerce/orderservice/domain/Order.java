package com.ecommerce.orderservice.domain;

import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.web.dto.OrderDto;
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
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    // Kafka KTable & KStream key
    @Column(unique = true)
    private String eventId;

    @Column(name = "user_id")
    private Long userId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItem> orderItems;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderStatus;

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
                .orderStatus(OrderItemStatus.WAITING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    public static Order fromFinalOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromFinalOrderItemDto)
                .collect(Collectors.toList());

        return Order.builder()
                .eventId(orderDto.getEventId())
                .userId(orderDto.getUserId())
                .orderItems(orderItems)
                .orderStatus(orderDto.getOrderStatus())
                .createdAt(orderDto.getCreatedAt())
                .requestedAt(orderDto.getRequestedAt())
                .build();
    }

    public void initializeEventId() {
        if(this.userId == null) {
            throw new OrderException(ExceptionCode.NOT_NULL_USER_ID);
        }
        String[] uuid = UUID.randomUUID().toString().split("-");
        this.eventId = this.userId.toString() + "-" + uuid[0];
    }

    public void updateOrderStatus(OrderItemStatus status) {
        this.orderStatus = status;
        this.orderItems
                .forEach(orderItem -> orderItem.updateOrderItemStatus(status));
    }

    public void updateOrderStatus(OrderDto orderDto) {
        this.orderStatus = orderDto.getOrderStatus();

        HashMap<Long, OrderItemStatus> orderItemStatusHashMap = new HashMap<>();
        orderDto.getOrderItemDtos()
                .forEach(o -> orderItemStatusHashMap.put(o.getItemId(), o.getOrderItemStatus()));

        this.orderItems
                .forEach(o -> o.updateOrderItemStatus(orderItemStatusHashMap.get(o.getItemId())));
    }
}
