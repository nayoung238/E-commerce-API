package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
//import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.repository.OrderRedisRepository;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * waiting 상태의 주문을 DB insert -> 재고 변경 결과 이벤트를 바탕으로 주문 상태를 update 하는 방식 (v1)
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
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public OrderDto create(OrderRequestDto orderRequestDto) {
        Order order = Order.of(orderRequestDto);
        order.initializeOrderEventId(getOrderEventId(order.getAccountId()));
        order.getOrderItems()
                .forEach(o -> o.initializeOrder(order));

        orderRepository.save(order);
        eventPublisher.publishEvent(order.getOrderCreationInternalEvent());
        return OrderDto.of(order);
    }

    @KafkaListener(topics = TopicConfig.ORDER_PROCESSING_RESULT_TOPIC)
    @Transactional
    public void updateOrderStatus(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventKey: {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderStatus());

        Order order = orderRepository.findByOrderEventId(record.value().getOrderEventId())
                        .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));
        order.updateOrderStatus(record.value());
    }

    @Override
//    @KafkaListener(topics = TopicConfig.REQUESTED_ORDER_TOPIC)
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderKafkaEvent> record) {
        if(record.value() != null) {
            log.info("Consuming message success -> Topic: {}, OrderEventKey: {}",
                    record.topic(),
                    record.value().getOrderEventId());

            try {
                waitBasedOnTimestamp(record.timestamp());

                Optional<Order> order = orderRepository.findByOrderEventId(record.value().getOrderEventId());
                if (order.isPresent()) {
                    if (Objects.equals(OrderStatus.WAITING, order.get().getOrderStatus())) {
                        kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC, null, record.value());
                    }
                } else kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, null, record.value());
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }

    private void waitBasedOnTimestamp(long recordTimestamp) throws InterruptedException {
        Instant recordAppendTime = Instant.ofEpochMilli(recordTimestamp);
        while (Instant.now().toEpochMilli() - recordAppendTime.toEpochMilli() < 3000) {
            Thread.sleep(1000);
        }
    }

    @Override
//    @KafkaListener(topics = TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC)
    public void requestOrderProcessingResult(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, order-event-key: {}",
                record.topic(),
                record.value().getOrderEventId());

        // OpenFeign
//        OrderStatus orderItemStatus = itemServiceClient.findOrderProcessingResult(record.value().getOrderEventId());
//        if(!orderItemStatus.equals(OrderStatus.NOT_EXIST))
//            updateOrderStatus(record.value().getOrderEventId(), orderItemStatus);
//        else resendKafkaMessage(null, record.value());
    }

    private void resendKafkaMessage(String key, OrderKafkaEvent value) {
        String[] redisKey = value.getRequestedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
        if(isFirstEvent(redisKey[0], value.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, key, value);
        else {
            updateOrderStatus(value.getOrderEventId(), OrderStatus.FAILED);
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    private boolean isFirstEvent(String key, String orderEventKey) {
        return orderRedisRepository.addOrderEventKey(key, orderEventKey) == 1;
    }

    private void updateOrderStatus(String orderEventKey, OrderStatus orderStatus) {
        Order order = orderRepository.findByOrderEventId(orderEventKey)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderStatus);
        orderRepository.save(order);
    }
}
