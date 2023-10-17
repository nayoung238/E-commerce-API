package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.messagequeue.KStreamKTableJoinConfig;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
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
        /*
            eventId(String) -> KTable & KStream key
            DTO 객체로 이벤트 생성하므로 이벤트 생성 시점에 order ID(PK) 값이 null
            -> customerAccountId 와 randomUUID 조합으로 unique한 값 생성
         */
        orderDto.initializeEventId();

        /*
           createdAt(LocalDateTime) -> 이벤트 중복 처리 판별에 사용하는 값
           Bean 생성하지 않고 DTO 객체로 이벤트 생성하므로 createdAt 직접 설정
         */
        orderDto.initializeCreatedAt();
        kafkaProducer.send(KStreamKTableJoinConfig.TEMPORARY_ORDER_TOPIC_NAME, orderDto.getEventId(), orderDto);
        return orderDto;
    }

    public void insertFinalOrderOnDB(OrderDto orderDto) {
        Order order = Order.fromFinalOrderDto(orderDto);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.setOrder(order);

        orderRepository.save(order);
    }

    /**
     * waiting 상태의 주문 생성(insert) -> 주문 상태 변경(update)하는 방식
     * 1개의 주문 생성에 대해 DB 두 번 접근 (insert -> update)
     */
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromTemporaryOrderDto(orderDto);
        order.initializeEventId();
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.setOrder(order);

        orderRepository.save(order);
        kafkaProducer.send(KStreamKTableJoinConfig.TEMPORARY_ORDER_TOPIC_NAME, null, OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatus(OrderDto orderDto) {
        Order order = orderRepository.findById(orderDto.getId())
                    .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.setOrderStatus(orderDto.getOrderStatus());

        HashMap<Long, OrderItemStatus> orderItemStatus = new HashMap<>();
        for(OrderItemDto orderItemDto : orderDto.getOrderItemDtos())
            orderItemStatus.put(orderItemDto.getItemId(), orderItemDto.getOrderItemStatus());

        for(OrderItem orderItem : order.getOrderItems())
            orderItem.setOrderItemStatus(orderItemStatus.get(orderItem.getItemId()));
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
