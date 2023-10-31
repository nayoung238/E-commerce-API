package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.messagequeue.KafkaProducerConfig;
import com.nayoung.orderservice.openfeign.ItemServiceClient;
import com.nayoung.orderservice.openfeign.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * waiting 상태의 주문을 DB insert -> 재고 변경 결과 이벤트를 바탕으로 주문 상태를 update 하는 방식 (v1)
 * 주문 생성을 위해 DB 두 번 접근 (insert -> update)
 */
@Service
@Slf4j
public class OrderServiceV1 extends OrderService {

    public OrderServiceV1(OrderRepository orderRepository, OrderRedisRepository orderRedisRepository,
                          KafkaProducer kafkaProducer,
                          ItemServiceClient itemServiceClient, CircuitBreakerFactory circuitBreakerFactory) {
        super(orderRepository, orderRedisRepository, kafkaProducer, itemServiceClient, circuitBreakerFactory);
    }

    @Override
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        Order order = Order.fromTemporaryOrderDto(orderDto);
        order.initializeEventId();

        order.getOrderItems()
                .forEach(o -> o.setOrder(order));

        orderRepository.save(order);
        kafkaProducer.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC, OrderDto.fromOrder(order));
        return OrderDto.fromOrder(order);
    }

    //@KafkaListener(topics = KStreamKTableJoinConfig.ITEM_UPDATE_RESULT_TOPIC)
    @Transactional
    public void updateOrderStatus(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Order Status: {}",
                record.topic(),
                record.value().getId(),
                record.value().getOrderStatus());

        updateOrderStatusByOrderDto(record.value());
    }

    private void updateOrderStatusByOrderDto(OrderDto orderDto) {
        Order order = orderRepository.findById(orderDto.getId())
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.setOrderStatus(orderDto.getOrderStatus());

        HashMap<Long, OrderItemStatus> orderItemStatusHashMap = new HashMap<>();
        orderDto.getOrderItemDtos()
                .forEach(o -> orderItemStatusHashMap.put(o.getItemId(), o.getOrderItemStatus()));

        order.getOrderItems()
                .forEach(o -> o.setOrderItemStatus(orderItemStatusHashMap.get(o.getItemId())));
    }

    @Override
    //@KafkaListener(topics = {KafkaProducerConfig.TEMPORARY_ORDER_TOPIC,
    //                         KafkaProducerConfig.TEMPORARY_RETRY_ORDER_TOPIC})
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                record.topic(),
                record.value().getId(),
                record.value().getEventId());

        try {
            waitBasedOnTimestamp(record.timestamp());

            Optional<Order> order = orderRepository.findByEventId(record.value().getEventId());
            if(order.isPresent()) {
                if(Objects.equals(OrderItemStatus.WAITING,order.get().getOrderStatus())) {
                    kafkaProducer.send(KafkaProducerConfig.RETRY_REQUEST_ORDER_ITEM_UPDATE_RESULT_TOPIC, record.value());
                }
            }
            else kafkaProducer.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC, record.value());
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    //@KafkaListener(topics = KafkaProducerConfig.RETRY_REQUEST_ORDER_ITEM_UPDATE_RESULT_TOPIC)
    public void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                record.topic(),
                record.value().getId(),
                record.value().getEventId());

        List<ItemUpdateLogDto> itemUpdateLogDtos = getAllOrderItemUpdateResultByEventId(record.value().getEventId());
        if(!itemUpdateLogDtos.isEmpty())
            updateOrderStatusByItemUpdateLogDtoList(record.value().getEventId(), itemUpdateLogDtos);
        else resendKafkaMessage(record.key(), record.value());
    }

    private void updateOrderStatusByItemUpdateLogDtoList(String eventId, List<ItemUpdateLogDto> itemUpdateLogDtoList) {
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
}
