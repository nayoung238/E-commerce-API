package com.nayoung.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import lombok.*;

import javax.persistence.*;

@Entity @Getter @Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;

    @Enumerated(EnumType.STRING)
    private OrderItemStatus orderItemStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public static OrderItem fromOrderItemDto(OrderItemDto orderItemDto) {
        return OrderItem.builder()
                .itemId(orderItemDto.getItemId())
                .quantity(orderItemDto.getQuantity())
                .price(orderItemDto.getPrice())
                .shopId(orderItemDto.getShopId())
                .build();
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

    public void updateOrderStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }
}
