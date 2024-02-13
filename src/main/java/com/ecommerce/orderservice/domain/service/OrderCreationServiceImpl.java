package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.OrderItemStatus;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerConfig;
import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.web.dto.OrderDto;
import com.ecommerce.orderservice.domain.Order;
import com.ecommerce.orderservice.domain.repository.OrderRedisRepository;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.*;

/**
 * waiting 상태의 주문을 DB insert -> 재고 변경 결과 이벤트를 바탕으로 주문 상태를 update 하는 방식 (v1)
 * 주문 생성을 위해 DB 두 번 접근 (insert -> update)
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderCreationServiceImpl implements OrderCreationService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducerService;
    private final ItemServiceClient itemServiceClient;

    @Override
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromTemporaryOrderDto(orderDto);
        order.initializeEventId();
        order.getOrderItems()
                .forEach(o -> o.setOrder(order));

        orderRepository.save(order);
        kafkaProducerService.send(KafkaProducerConfig.REQUESTED_ORDER_TOPIC, null, OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

//    @KafkaListener(topics = KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_TOPIC)
    @Transactional
    public void updateOrderStatus(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, order Id: {}, event Id: {}, Order Status: {}",
                record.topic(),
                record.value().getId(),
                record.value().getEventId(),
                record.value().getOrderStatus());

        Optional<Order> order = orderRepository.findById(record.value().getId());
        order.ifPresent(value -> value.updateOrderStatus(record.value()));
    }

    @Override
//    @KafkaListener(topics = KafkaProducerConfig.REQUESTED_ORDER_TOPIC)
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record) {
        if(record.value() != null) {
            log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                    record.topic(),
                    record.value().getId(),
                    record.value().getEventId());

            try {
                waitBasedOnTimestamp(record.timestamp());

                Optional<Order> order = orderRepository.findById(record.value().getId());
                if (order.isPresent()) {
                    if (Objects.equals(OrderItemStatus.WAITING, order.get().getOrderStatus())) {
                        kafkaProducerService.send(KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC, null, record.value());
                    }
                } else kafkaProducerService.send(KafkaProducerConfig.REQUESTED_ORDER_TOPIC, null, record.value());
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
//    @KafkaListener(topics = KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_TOPIC)
    public void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                record.topic(),
                record.value().getId(),
                record.value().getEventId());

        // OpenFeign
        OrderItemStatus orderItemStatus = itemServiceClient.findOrderProcessingResultByEventId(record.value().getEventId());
        if(!orderItemStatus.equals(OrderItemStatus.NOT_EXIST))
            updateOrderStatusByEventId(record.value().getEventId(), orderItemStatus);
        else resendKafkaMessage(null, record.value());
    }

    private void resendKafkaMessage(String key, OrderDto value) {
        String[] redisKey = value.getRequestedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
        if(isFirstEvent(redisKey[0], value.getEventId()))
            kafkaProducerService.send(KafkaProducerConfig.REQUESTED_ORDER_TOPIC, key, value);
        else {
            updateOrderStatusByEventId(value.getEventId(), OrderItemStatus.FAILED);
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    private boolean isFirstEvent(String key, String eventId) {
        return orderRedisRepository.addEventId(key, eventId) == 1;
    }

    private void updateOrderStatusByEventId(String eventId, OrderItemStatus orderItemStatus) {
        Order order = orderRepository.findByEventId(eventId)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderItemStatus);
        orderRepository.save(order);
    }
}
