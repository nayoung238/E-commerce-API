package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;

public interface OrderService {

    OrderResponse create(OrderRequest orderRequest);
    OrderResponse findOrderByOrderId(Long orderId);
    void updateOrderStatus(OrderStatus orderStatus, Long orderId);
}
