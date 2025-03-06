package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.common.exception.CustomException;
import com.ecommerce.orderservice.order.dto.OrderSimpleDto;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.order.dto.OrderDto;
import com.ecommerce.orderservice.common.exception.ErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderInquiryService {

    public final OrderRepository orderRepository;
    public static final int PAGE_SIZE = 5;

    public OrderDto findOrderById(Long orderId, Long userId) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ORDER));

        if (!order.getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        return OrderDto.of(order);
    }

    public OrderDto findLatestOrderByUserId(Long userId) {
        Order order = orderRepository.findLatestOrderByUserId(userId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ORDER.getMessage()));

        return OrderDto.of(order);
    }

    public OrderDto findOrderByOrderEventId(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ORDER.getMessage()));
        return OrderDto.of(order);
    }

    public List<OrderSimpleDto> findOrdersByUserIdAndOrderId(Long userId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        List<Order> orders;
        if(orderId != null) {
            orders = orderRepository.findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(userId, orderId, pageRequest);
        }
        else {
            orders = orderRepository.findByUserIdOrderByOrderIdDesc(userId, pageRequest);
        }

        return orders.stream()
            .sorted(Comparator.comparing(Order::getId).reversed())
            .map(OrderSimpleDto::of)
            .toList();
    }
}
