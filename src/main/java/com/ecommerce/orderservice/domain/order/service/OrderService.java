package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.dto.OrderListDto;
import com.ecommerce.orderservice.domain.order.dto.OrderSimpleDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    public final OrderRepository orderRepository;
    public static final int PAGE_SIZE = 5;

    public OrderListDto findOrderByUserIdAndOrderId(Long userId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        List<Order> orders;
        if(orderId != null)
            orders = orderRepository.findByUserIdAndOrderIdLessThanOrderByOrderIdDesc(userId, orderId, pageRequest);
        else
            orders = orderRepository.findByUserIdOrderByOrderIdDesc(userId, pageRequest);

        List<OrderSimpleDto> orderSimpleDtoList =  orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderSimpleDto::of)
                .toList();

        return new OrderListDto(orderSimpleDtoList);
    }

    @Transactional
    public OrderDto findOrderByOrderEventId(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));
        return OrderDto.of(order);
    }
}
