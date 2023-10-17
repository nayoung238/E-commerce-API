package com.nayoung.accountservice.web.dto;

import com.nayoung.accountservice.client.OrderItemStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrderResponse {
    private Long orderId;
    private OrderItemStatus orderStatus;

    private List<OrderItemResponse> orderItemResponses;
    private Long totalPrice;

    private Long customerAccountId;
    private LocalDateTime createdAt;
}
