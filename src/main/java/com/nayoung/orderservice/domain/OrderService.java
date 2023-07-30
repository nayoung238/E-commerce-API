package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromOrderDto(orderDto);
        Order savedOrder = orderRepository.save(order);
        return OrderDto.fromOrder(savedOrder);
    }

    public OrderDto findOrderByOrderId(Order.OrderPK id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatus(OrderStatus orderStatus, Order.OrderPK id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderStatus);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.updateOrderStatus(orderStatus);
    }

    public List<OrderDto> findOrderByCustomerAccountIdAndOrderId(Long customerAccountId, Long cursorOrderId) {
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Order> orders = new ArrayList<>();
        if(cursorOrderId != null)
            orders = orderRepository.findByCustomerAccountIdAndIdLessThanOrderByIdDesc(customerAccountId, cursorOrderId, pageRequest);
        else
            orders = orderRepository.findByCustomerAccountIdOrderByIdDesc(customerAccountId, pageRequest);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderDto::fromOrder)
                .collect(Collectors.toList());
    }
}
