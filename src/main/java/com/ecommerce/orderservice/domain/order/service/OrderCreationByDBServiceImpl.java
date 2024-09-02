package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
//import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.repository.OrderRedisRepository;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * waiting 상태의 주문을 DB insert -> 재고 변경 결과 이벤트로 주문 상태 update 하는 방식
 * 주문 생성을 위해 DB 두 번 접근 (insert -> update)
 */
@Service @Primary
@Slf4j
@RequiredArgsConstructor
public class OrderCreationByDBServiceImpl implements OrderCreationService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducerService;
//    private final ItemServiceClient itemServiceClient;
    private final InternalEventService internalEventService;

    @Override
    @Transactional
    public OrderDto create(OrderRequestDto orderRequestDto) {
        Order order = Order.of(orderRequestDto);
        order.initializeOrderEventId(getOrderEventId(order.getAccountId()));
        order.getOrderItems()
                .forEach(o -> o.initializeOrder(order));

        orderRepository.save(order);
        internalEventService.publishInternalEvent(order.getOrderCreationInternalEvent());
        return OrderDto.of(order);
    }

    @Transactional
    public void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        Order order = orderRepository.findByOrderEventId(orderKafkaEvent.getOrderEventId())
                        .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ORDER.getMessage()));
        order.updateOrderStatus(orderKafkaEvent);
    }

    @Override
    public void checkFinalStatusOfOrder(OrderKafkaEvent orderKafkaEvent, long recordTimestamp) {
        delayFromTimestamp(recordTimestamp);

        Optional<Order> order = orderRepository.findByOrderEventId(orderKafkaEvent.getOrderEventId());
        if(order.isPresent()) {
            if(Objects.equals(OrderStatus.WAITING, order.get().getOrderStatus())) {
                kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC, null, orderKafkaEvent);
            }
        } else kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, null, orderKafkaEvent);
    }

    @Override
    public void requestOrderProcessingResult(OrderKafkaEvent orderKafkaEvent) {
//        OrderStatus orderStatus = itemServiceClient.findOrderProcessingResult(orderKafkaEvent.getOrderEventId());
//        if(orderStatus.equals(OrderStatus.SUCCEEDED) || orderStatus.equals(OrderStatus.FAILED))
//            updateOrderStatus(orderKafkaEvent.getOrderEventId(), orderStatus);
//        else resendKafkaMessage(orderKafkaEvent);
    }

    @Override
    @Transactional
    public void resendKafkaMessage(OrderKafkaEvent orderKafkaEvent) {
        String redisKey = getRedisKey(orderKafkaEvent.getRequestedAt());
        if(isFirstEvent(redisKey, orderKafkaEvent.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, null, orderKafkaEvent);
        else {
            updateOrderStatus(orderKafkaEvent.getOrderEventId(), OrderStatus.FAILED);
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    @Override
    public boolean isFirstEvent(String redisKey, String orderEventId) {
        return orderRedisRepository.addOrderEventKey(redisKey, orderEventId) == 1;
    }

    @Override
    public void updateOrderStatus(String orderEventId, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ORDER.getMessage()));

        order.updateOrderStatus(orderStatus);
        orderRepository.save(order);
    }
}
