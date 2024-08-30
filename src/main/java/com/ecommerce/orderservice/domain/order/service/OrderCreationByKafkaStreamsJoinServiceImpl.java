package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
//import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * KStream(주문에 대한 재고 변경 결과) + KTable(waiting 상태의 주문) Join 한 결과(주문 상세)를 DB에 insert 하는 방식 (v2)
 * 주문 생성을 위해 DB 한 번 접근 (insert)
 */
@Service //@Primary
@Slf4j
@RequiredArgsConstructor
public class OrderCreationByKafkaStreamsJoinServiceImpl implements OrderCreationService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducerService;
//    private final ItemServiceClient itemServiceClient;

    @Override
    @Transactional
    public OrderDto create(OrderRequestDto orderRequestDto) {
        String orderEventId = getOrderEventId(orderRequestDto.getAccountId());
        OrderKafkaEvent orderKafkaEvent = OrderKafkaEvent.of(orderRequestDto, orderEventId);
        kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
        return OrderDto.of(orderKafkaEvent);
    }

    @KafkaListener(topics = TopicConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC)
    public void create(ConsumerRecord<String, OrderKafkaEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventKey: {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventId(),
                record.value().getOrderStatus());

        if(!isExistOrderByOrderEventId(record.key())) {
            Order order = Order.of(record.value());
            orderRepository.save(order);
            kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, record.key());
        }
    }

    @Override
    @KafkaListener(topics = TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC)
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderKafkaEvent> record) {
        if(record.key() != null && record.value() != null) {
            log.info("Consuming message success -> Topic: {}, Key(event Id): {}",
                    record.topic(),
                    record.key());

            try {
                waitBasedOnTimestamp(record.timestamp());

                if (!isExistOrderByOrderEventId(record.key())) {
                    kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC, record.key(), record.value());
                }
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
    @KafkaListener(topics = TopicConfig.ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC)
    public void requestOrderProcessingResult(ConsumerRecord<String, OrderKafkaEvent> record) {
        assert record.key() != null & record.value() != null;
        log.info("Consuming message success -> Topic: {}, OrderEventKey: {}",
                record.topic(),
                record.key());

        // OpenFeign
//        OrderStatus orderStatus = itemServiceClient.findOrderProcessingResult(record.key());
//        if (orderStatus.equals(OrderStatus.SUCCEEDED) || orderStatus.equals(OrderStatus.FAILED)) {
//            if(!isExistOrderByOrderEventId(record.key())) {
//                OrderKafkaEvent orderEvent = OrderKafkaEvent.of(record.key(), orderStatus);
//                kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, record.key(), orderEvent);
//            }
//        } else if (orderStatus.equals(OrderStatus.SERVER_ERROR)) {
//            kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, record.key());
//        } else {
//            resendKafkaMessage(record.key(), record.value());
//        }
    }

    private void resendKafkaMessage(String key, OrderKafkaEvent orderEvent) {
        String[] redisKey = orderEvent.getRequestedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
        if(isFirstEvent(redisKey[0], orderEvent.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, key, orderEvent);
        else {
            updateOrderStatusByOrderEventId(orderEvent.getOrderEventId(), OrderStatus.FAILED);
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    private boolean isFirstEvent(String key, String orderEventKey) {
        return orderRedisRepository.addOrderEventKey(key, orderEventKey) == 1;
    }

    private void updateOrderStatusByOrderEventId(String orderEventKey, OrderStatus orderStatus) {
        if(!isExistOrderByOrderEventId(orderEventKey)) {
            OrderKafkaEvent orderEvent = OrderKafkaEvent.of(orderEventKey, orderStatus);
            kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, orderEventKey, orderEvent);
        }
    }

    private boolean isExistOrderByOrderEventId(String orderEventId) {
        return orderRepository.existsByOrderEventId(orderEventId);
    }
}
