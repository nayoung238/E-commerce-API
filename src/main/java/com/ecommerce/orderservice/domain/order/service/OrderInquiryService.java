package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.dto.OrderListDto;
import com.ecommerce.orderservice.domain.order.dto.OrderSimpleDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.exception.ExceptionCode;
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

    public OrderDto findLatestOrderByAccountId(Long accountId) {
        Order order = orderRepository.findLatestOrderByAccountId(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ORDER.getMessage()));

        return OrderDto.of(order);
    }

    public OrderDto findOrderByOrderEventId(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ORDER.getMessage()));
        return OrderDto.of(order);
    }

    public OrderListDto findOrderByAccountIdAndOrderId(Long accountId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, PAGE_SIZE);
        List<Order> orders;
        if(orderId != null)
            orders = orderRepository.findByAccountIdAndOrderIdLessThanOrderByOrderIdDesc(accountId, orderId, pageRequest);
        else
            orders = orderRepository.findByAccountIdOrderByOrderIdDesc(accountId, pageRequest);

        List<OrderSimpleDto> orderSimpleDtoList =  orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderSimpleDto::of)
                .toList();

        return new OrderListDto(orderSimpleDtoList);
    }
}
