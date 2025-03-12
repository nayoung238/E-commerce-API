package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.common.exception.CustomException;
import com.ecommerce.orderservice.order.dto.response.OrderSummaryResponse;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.common.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderQueryService {

    public final OrderRepository orderRepository;
    public static final int PAGE_SIZE = 5;

    public OrderDetailResponse findOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ORDER));

        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return OrderDetailResponse.of(order);
    }

    public OrderDetailResponse findOrderByOrderEventId(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ORDER.getMessage()));
        return OrderDetailResponse.of(order);
    }

    public List<OrderSummaryResponse> findOrdersByUserIdAndOrderId(Long userId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        List<Order> orders = orderRepository.findOrdersWithCursor(userId, orderId, pageRequest);

        return orders.stream()
            .sorted(Comparator.comparing(Order::getId).reversed())
            .map(OrderSummaryResponse::of)
            .toList();
    }
}
