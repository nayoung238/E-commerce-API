package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderItemRequest;
import com.nayoung.orderservice.web.dto.OrderRequest;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity @Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "orders")

@IdClass(Order.OrderPK.class)
public class Order {

    @Id
    @Column(name = "customer_account_id")
    private Long customerAccountId;

    @SequenceGenerator(
            name = "orders", sequenceName = "order_entity",
            initialValue = 1, allocationSize = 10
    )
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "orders")
    @Column(name = "order_id")
    private Long id;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private LocalDateTime createdAt;

    private Order(Long customerAccountId, List<OrderItem> orderItems) {
        for(OrderItem orderItem : orderItems) {
            this.orderItems.add(orderItem);
            orderItem.setOrder(this);
        }
        this.orderStatus = OrderStatus.ACCEPTED;
        this.customerAccountId = customerAccountId;
        this.createdAt = LocalDateTime.now();
    }

    protected static Order fromOrderRequest(OrderRequest request) {
        return new Order(request.getCustomerAccountId(), getOrderItem(request.getOrderItems()));
    }

    @NoArgsConstructor
    public static class OrderPK implements Serializable {
        private Long customerAccountId;
        private Long id;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }

    private static List<OrderItem> getOrderItem(List<OrderItemRequest> requests) {
        return requests.parallelStream()
                .map(OrderItem::new)
                .collect(Collectors.toList());
    }
}
