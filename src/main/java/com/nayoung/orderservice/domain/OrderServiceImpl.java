package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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
    public OrderResponse findOrderByOrderId(Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderResponse.fromOrderEntity(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(OrderStatus orderStatus, Long orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderStatus);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.updateOrderStatus(orderStatus);
    }
}
