package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.exception.OrderStatusException;
import com.nayoung.orderservice.messagequeue.KStreamKTableJoinConfig;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.messagequeue.client.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service @Slf4j
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final KafkaProducer kafkaProducer;

    /**
     * 주문에 대한 재고 변경 결과(KStream) + waiting 상태의 주문(KTable)을 Join한 결과를 DB에 insert 하는 방식
     * 1개의 주문 생성에 대해 DB 한 번 접근 (KStream, KTable Join 결과를 insert)
     */
    @Transactional
    public OrderDto createByKStream(OrderDto orderDto) {
        String KTableKey = orderDto.initializeEventId();
        kafkaProducer.send(KStreamKTableJoinConfig.TEMPORARY_ORDER_TOPIC_NAME, KTableKey, orderDto);
        return orderDto;
    }

    public void insertFinalOrderOnDB(OrderDto orderDto) {
        Order order = Order.fromOrderDto(orderDto);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.setOrder(order);

        orderRepository.save(order);
    }

    /**
     * waiting 상태의 주문 생성(insert) -> 주문 상태 변경(update)하는 방식
     * 1개의 주문 생성에 대해 DB 두 번 접근 (insert -> update)
     */
    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromOrderDto(orderDto);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.setOrder(order);

        order = orderRepository.save(order);

        kafkaProducer.send(KStreamKTableJoinConfig.TEMPORARY_ORDER_TOPIC_NAME, null, OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

    public OrderDto findOrderByOrderId(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatus(List<ItemUpdateLogDto> itemUpdateLogDtos) {
        assert itemUpdateLogDtos != null;
        try {
            Long orderId = itemUpdateLogDtos.get(0).getOrderId();
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

            HashMap<Long, OrderItemStatus> orderItemStatus = new HashMap<>();
            for(ItemUpdateLogDto itemUpdateLogDto : itemUpdateLogDtos)
                orderItemStatus.put(itemUpdateLogDto.getItemId(), itemUpdateLogDto.getOrderItemStatus());

            for(OrderItem orderItem : order.getOrderItems())
                orderItem.updateOrderStatus(orderItemStatus.get(orderItem.getItemId()));

            boolean isAllSucceeded = order.getOrderItems().stream()
                     .allMatch(o -> Objects.equals(o.getOrderItemStatus(), OrderItemStatus.SUCCEEDED));
            if(isAllSucceeded) order.updateOrderStatus(OrderItemStatus.SUCCEEDED);
            else order.updateOrderStatus(OrderItemStatus.FAILED);
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
