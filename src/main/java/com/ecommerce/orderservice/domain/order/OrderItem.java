package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
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
    private OrderStatus orderStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public static OrderItem fromTemporaryOrderItemDto(OrderItemDto orderItemDto) {
        return OrderItem.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderStatus(OrderStatus.WAITING)
                .build();
    }

    public static OrderItem fromFinalOrderItemDto(OrderItemDto orderItemDto) {
        return OrderItem.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .orderStatus(orderItemDto.getOrderStatus())
                .build();
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
