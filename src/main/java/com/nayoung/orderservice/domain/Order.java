package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity @Getter @Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
@EntityListeners(AuditingEntityListener.class)
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(unique = true)
    private String eventId;

    @Column(name = "customer_account_id")
    private Long customerAccountId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderStatus;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime requestedAt;

    protected static Order fromTemporaryOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromTemporaryOrderItemDto)
                .collect(Collectors.toList());

        return Order.builder()
                .customerAccountId(orderDto.getCustomerAccountId())
                .orderItems(orderItems)
                .orderStatus(OrderItemStatus.WAITING)
                .requestedAt(LocalDateTime.now())
                .build();
    }

    protected static Order fromFinalOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromFinalOrderItemDto)
                .collect(Collectors.toList());

        return Order.builder()
                .eventId(orderDto.getEventId())
                .customerAccountId(orderDto.getCustomerAccountId())
                .orderItems(orderItems)
                .orderStatus(orderDto.getOrderStatus())
                .createdAt(orderDto.getCreatedAt())
                .requestedAt(orderDto.getRequestedAt())
                .build();
    }

    public void setOrderStatus(OrderItemStatus status) {
        this.orderStatus = status;
    }

    protected void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
