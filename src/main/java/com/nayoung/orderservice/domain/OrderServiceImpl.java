package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
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
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepository;

    @Override
    public OrderResponse create(OrderRequest orderRequest) {
        Order order = Order.fromOrderRequest(orderRequest);
        Order savedOrder = orderRepository.save(order);
        return OrderResponse.fromOrderEntity(savedOrder);
    }

    @Override
    public OrderResponse findOrderByOrderId(Order.OrderPK id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderResponse.fromOrderEntity(order);
    }

    @Override
    @Transactional
    public void updateOrderStatus(OrderStatus orderStatus, Order.OrderPK id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderStatus);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.updateOrderStatus(orderStatus);
    }

    @Override
    public List<OrderResponse> findOrderByCustomerAccountIdAndOrderId(Long customerAccountId, Long cursorOrderId) {
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Order> orders = new ArrayList<>();
        if(cursorOrderId != null)
            orders = orderRepository.findByCustomerAccountIdAndIdLessThanOrderByIdDesc(customerAccountId, cursorOrderId, pageRequest);
        else
            orders = orderRepository.findByCustomerAccountIdOrderByIdDesc(customerAccountId, pageRequest);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderResponse::fromOrderEntity)
                .collect(Collectors.toList());
    }
}
