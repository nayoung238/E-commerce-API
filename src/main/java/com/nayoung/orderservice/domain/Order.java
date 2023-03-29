package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private Long itemId;
    private Long quantity;
    private Long unitPrice;
    private Long totalPrice;

    private Long accountId;

    private LocalDateTime createdAt;

    private Order(OrderRequest request) {
        this.orderStatus = OrderStatus.ACCEPT;

        this.itemId = request.getItemId();
        this.quantity = request.getQuantity();
        this.unitPrice = request.getUnitPrice();
        this.totalPrice = request.getTotalPrice();

        this.accountId = request.getAccountId();

        this.createdAt = LocalDateTime.now();
    }

    protected static Order fromOrderRequest(OrderRequest orderRequest) {
        return new Order(orderRequest);
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
