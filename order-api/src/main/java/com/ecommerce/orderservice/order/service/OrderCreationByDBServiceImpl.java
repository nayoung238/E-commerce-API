package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.common.exception.ErrorCode;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
//import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.order.dto.OrderDto;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.repository.OrderRedisRepository;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * 1. waiting 상태의 주문을 DB insert
 * 2. 재고 변경 결과 이벤트를 받아 주문 상태 update
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
        order.initializeOrderEventId(getOrderEventId(order.getUserId()));
        orderRepository.save(order);

        // Kafka 이벤트 발행을 위한 내부 이벤트 생성 (Transactional Outbox Pattern)
        internalEventService.publishInternalEvent(order.getOrderInternalEvent());
        return OrderDto.of(order);
    }

    @Transactional
    public void updateOrderStatus(OrderKafkaEvent orderKafkaEvent) {
        Optional<Order> order = orderRepository.findByOrderEventId(orderKafkaEvent.getOrderEventId());
        if (order.isPresent()) {
            order.get().updateOrderStatus(orderKafkaEvent);
        }
        else {
            /*
                TODO: DB 영속화 실패, but Kafka 이벤트 발행되어 상품 서비스에서 처리된 상황
                상품 서비스로 요청 처리 취소 발행
             */
        }
    }

    @Override
    public void checkFinalStatusOfOrder(OrderKafkaEvent orderKafkaEvent, long recordTimestamp) {
        delayFromTimestamp(recordTimestamp);

        Optional<Order> order = orderRepository.findByOrderEventId(orderKafkaEvent.getOrderEventId());
        if(order.isPresent()) {
            // 주문 요청은 영속화, but 처리 지연 발생
            if(Objects.equals(OrderProcessingStatus.PROCESSING, order.get().getOrderProcessingStatus())) {
                kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC, null, orderKafkaEvent);
            }
        }
        // DB 영속화 실패했는데, Kafka 이벤트만 발행된 경우
        else {
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, null, orderKafkaEvent);
        }
    }

    @Override
    @Transactional
    public void requestOrderProcessingResult(OrderKafkaEvent orderKafkaEvent) {
//        OrderProcessingStatus status = itemServiceClient.findOrderProcessingResult(orderKafkaEvent.getOrderEventId());
//        if(status.equals(OrderProcessingStatus.SUCCESSFUL)) {
//            handleOrderSuccess(orderKafkaEvent.getOrderEventId());
//        }
//        else if (status.equals(OrderProcessingStatus.FAILED)) {
//            handleOrderFailure(orderKafkaEvent.getOrderEventId());
//        }
//        else resendKafkaMessage(orderKafkaEvent);
    }

    private void handleOrderSuccess(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ORDER.getMessage()));

        order.updateOrderStatus(OrderProcessingStatus.SUCCESSFUL);
    }

    @Override
    @Transactional
    public void resendKafkaMessage(OrderKafkaEvent orderKafkaEvent) {
        String redisKey = getRedisKey(orderKafkaEvent.getRequestedAt());
        if(isFirstEvent(redisKey, orderKafkaEvent.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
        else {
            handleOrderFailure(orderKafkaEvent.getOrderEventId());
        }
    }

    @Override
    public boolean isFirstEvent(String redisKey, String orderEventId) {
        return orderRedisRepository.addOrderEventKey(redisKey, orderEventId) == 1;
    }

    @Override
    @Transactional
    public void handleOrderFailure(String orderEventId) {
        Order order = orderRepository.findByOrderEventId(orderEventId)
                .orElseThrow(() -> new EntityNotFoundException(ErrorCode.NOT_FOUND_ORDER.getMessage()));

        order.updateOrderStatus(OrderProcessingStatus.CANCELED);
        internalEventService.publishInternalEvent(order.getOrderInternalEvent());
    }
}
