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
    private String eventId;

    @Column(name = "customer_account_id")
    private Long customerAccountId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderStatus;

    private Long totalPrice;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    protected static Order fromTemporaryOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromTemporaryOrderItemDto)
                .collect(Collectors.toList());

        return Order.builder()
                .customerAccountId(orderDto.getCustomerAccountId())
                .orderItems(orderItems)
                .orderStatus(OrderItemStatus.WAITING)
                .totalPrice(getTotalPrice(orderItems))
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
                .totalPrice(getTotalPrice(orderItems))
                .createdAt(orderDto.getCreatedAt())
                .build();
    }

    public void setOrderStatus(OrderItemStatus status) {
        this.orderStatus = status;
    }

    private static Long getTotalPrice(List<OrderItem> orderItems) {
        assert(!orderItems.isEmpty());
        return orderItems.stream()
                .map(OrderItem::getPrice)
                .reduce(Long::sum)
                .get();
    }

    public void initializeEventId() {
        eventId = customerAccountId.toString() + ":" + UUID.randomUUID();
    }
}
