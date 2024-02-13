package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.Order;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    public final OrderRepository orderRepository;
    public static final int PAGE_SIZE = 5;

    public List<OrderDto> findOrderByUserIdAndOrderId(Long userId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        List<Order> orders;
        if(orderId != null)
            orders = orderRepository.findByUserIdAndIdLessThanOrderByIdDesc(userId, orderId, pageRequest);
        else
            orders = orderRepository.findByUserIdOrderByIdDesc(userId, pageRequest);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderDto::fromOrder)
                .collect(Collectors.toList());
    }
}
