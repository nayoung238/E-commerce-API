package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;

import java.util.List;

public interface OrderService {

    OrderResponse create(OrderRequest orderRequest);
    OrderResponse findOrderByOrderId(Order.OrderPK id);
    void updateOrderStatus(OrderStatus orderStatus, Order.OrderPK id);
    List<OrderResponse> findOrderByCustomerAccountIdAndOrderId(Long customerAccountId, Long cursorOrderId);
}
