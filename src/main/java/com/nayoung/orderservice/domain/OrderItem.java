package com.nayoung.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;

    private Long quantity;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public static OrderItem fromTemporaryOrderItemDto(OrderItemDto orderItemDto) {
        return OrderItem.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderItemStatus(OrderItemStatus.WAITING)
                .build();
    }

    public static OrderItem fromFinalOrderItemDto(OrderItemDto orderItemDto) {
        return OrderItem.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderItemStatus(orderItemDto.getOrderItemStatus())
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void setOrderItemStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }
}
