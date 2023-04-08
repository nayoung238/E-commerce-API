package com.nayoung.orderservice.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nayoung.orderservice.web.dto.OrderItemRequest;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity @Getter
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
    private OrderStatus orderStatus;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    public OrderItem(OrderItemRequest request) {
        this.itemId = request.getItemId();
        this.quantity = request.getQuantity();
        this.price = request.getPrice();
        this.shopId = request.getShopId();
        this.orderStatus = OrderStatus.WAITING;
    }

    protected void setOrder(Order order) {
        this.order = order;
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
