package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.exception.OrderStatusException;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.messagequeue.client.ItemUpdateStatus;
import com.nayoung.orderservice.web.dto.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRedisRepository stockRedisRepository;
    private final KafkaProducer kafkaProducer;

    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromOrderDto(orderDto);
        order = orderRepository.save(order);

        kafkaProducer.send("e-commerce.order.order-details", OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

    public OrderDto findOrderByOrderId(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatus(List<ItemUpdateLogDto> itemUpdateLogDtos) {
        try {
            Long orderId = itemUpdateLogDtos.get(0).getOrderId();
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

            HashMap<Long, ItemUpdateStatus> orderItemStatus = new HashMap<>();
            for(ItemUpdateLogDto itemUpdateLogDto : itemUpdateLogDtos)
                orderItemStatus.put(itemUpdateLogDto.getItemId(), itemUpdateLogDto.getItemUpdateStatus());

            for(OrderItem orderItem : order.getOrderItems()) {
                OrderStatus orderStatus = OrderStatus.getOrderStatus(orderItemStatus.get(orderItem.getItemId()));
                orderItem.updateOrderStatus(orderStatus);
            }
            boolean isAllSucceeded = order.getOrderItems().stream()
                     .allMatch(o -> Objects.equals(o.getOrderStatus(), OrderStatus.SUCCEEDED));
            if(isAllSucceeded) order.updateOrderStatus(OrderStatus.SUCCEEDED);
            else order.updateOrderStatus(OrderStatus.FAILED);
        } catch(OrderStatusException e) {
           e.printStackTrace();
        }
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
