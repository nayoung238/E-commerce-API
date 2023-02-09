package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;

    @Override
    public OrderResponse create(OrderRequest orderRequest) {
        Order order = Order.fromOrderRequest(orderRequest);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromOrderEntity(savedOrder);
    }

    @Override
    public OrderResponse getOrderByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId).orElseThrow();
        return OrderResponse.fromOrderEntity(order);
    }

    @Override
    public List<OrderResponse> getOrdersByAccountId(Long accountId) {
        List<Order> orders = orderRepository.findAllByAccountId(accountId);

        List<OrderResponse> ordersResponse = new ArrayList<>();
        orders.forEach(o ->  ordersResponse.add(OrderResponse.fromOrderEntity(o)));
        return ordersResponse;
    }
}
