package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity @Getter @Builder
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

    private Order(Long customerAccountId, List<OrderItem> orderItems) {
        for(OrderItem orderItem : orderItems) {
            this.getOrderItems().add(orderItem);
            orderItem.setOrder(this);
        }
        this.customerAccountId = customerAccountId;
        this.orderStatus = OrderStatus.WAITING;
    }

    protected static Order fromOrderDto(OrderDto orderDto) {
        List<OrderItem> orderItems = orderDto.getOrderItemDtos().stream()
                .map(OrderItem::fromOrderItemDto)
                .collect(Collectors.toList());

        return new Order(orderDto.getCustomerAccountId(), orderItems);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    public static class OrderPK implements Serializable {
        private Long customerAccountId;
        private Long id;
    }

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
