package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.messagequeue.KafkaProducerConfig;
import com.nayoung.orderservice.messagequeue.openFeign.ItemUpdateLogDto;
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
        kafkaProducer.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC_NAME, orderDto.getEventId(), orderDto);
        return orderDto;
    }

    @Transactional
    public void insertFinalOrderOnDB(OrderDto orderDto) {
        Order order = Order.fromFinalOrderDto(orderDto);
        order.getOrderItems()
                .forEach(o -> o.setOrder(order));

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

        order.getOrderItems()
                .forEach(o -> o.setOrder(order));

        orderRepository.save(order);
        kafkaProducer.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC_NAME, null, OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatusByOrderDto(OrderDto orderDto) {
        Order order = orderRepository.findById(orderDto.getId())
                    .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.setOrderStatus(orderDto.getOrderStatus());

        HashMap<Long, OrderItemStatus> orderItemStatusHashMap = new HashMap<>();
        orderDto.getOrderItemDtos()
                .forEach(o -> orderItemStatusHashMap.put(o.getItemId(), o.getOrderItemStatus()));

        order.getOrderItems()
                .forEach(o -> o.setOrderItemStatus(orderItemStatusHashMap.get(o.getItemId())));
    }

    @Transactional
    public void updateOrderStatusByItemUpdateLogDtoList(String eventId, List<ItemUpdateLogDto> itemUpdateLogDtoList) {
        Order order = orderRepository.findByEventId(eventId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        HashMap<Long, OrderItemStatus> orderItemStatusHashMap = new HashMap<>();
        itemUpdateLogDtoList
                .forEach(i -> orderItemStatusHashMap.put(i.getItemId(), i.getOrderItemStatus()));

        order.getOrderItems()
                .forEach(o -> o.setOrderItemStatus(orderItemStatusHashMap.get(o.getItemId())));

        boolean isAllSucceeded = order.getOrderItems().stream()
                .allMatch(o -> Objects.equals(OrderItemStatus.SUCCEEDED, o.getOrderItemStatus()));
        if(isAllSucceeded) order.setOrderStatus(OrderItemStatus.SUCCEEDED);
        else order.setOrderStatus(OrderItemStatus.FAILED);
    }

    @Transactional
    public void updateOrderStatusToFailedByEventId(String eventId) {
        Order order = orderRepository.findByEventId(eventId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.setOrderStatus(OrderItemStatus.FAILED);
        order.getOrderItems()
                .forEach(o -> o.setOrderItemStatus(OrderItemStatus.FAILED));
    }

    public List<OrderDto> findOrderByCustomerAccountIdAndOrderId(Long customerAccountId, Long orderId) {
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Order> orders;
        if(orderId != null)
            orders = orderRepository.findByCustomerAccountIdAndIdLessThanOrderByIdDesc(customerAccountId, orderId, pageRequest);
        else
            orders = orderRepository.findByCustomerAccountIdOrderByIdDesc(customerAccountId, pageRequest);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderDto::fromOrder)
                .collect(Collectors.toList());
    }
}
