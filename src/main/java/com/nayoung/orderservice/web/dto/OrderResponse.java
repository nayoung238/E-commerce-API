package com.nayoung.orderservice.web.dto;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderStatus;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Data
public class OrderResponse {

    private Long orderId;
    private OrderStatus orderStatus;

    private List<OrderItemResponse> orderItemResponses;
    private Long totalPrice;

    private Long customerAccountId;
    private LocalDateTime createdAt;


    private OrderResponse(Order order, List<OrderItemResponse> orderItemResponses) {
        this.orderId = order.getId();
        this.orderStatus = order.getOrderStatus();
        this.orderItemResponses = orderItemResponses;
        this.customerAccountId = order.getCustomerAccountId();
        this.createdAt = order.getCreatedAt();
    }

    public static OrderResponse fromOrderEntity(Order order) {
        List<OrderItemResponse> orderItemResponses = order.getOrderItems().parallelStream()
                .map(OrderItemResponse::new)
                .collect(Collectors.toList());

        return new OrderResponse(order, orderItemResponses);
    }
}
