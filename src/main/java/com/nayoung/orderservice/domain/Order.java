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
public class Order {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id")
    private Long id;

    @Column(name = "customer_account_id")
    private Long customerAccountId;

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

    public void updateOrderStatus(OrderStatus status) {
        this.orderStatus = status;
    }
}
