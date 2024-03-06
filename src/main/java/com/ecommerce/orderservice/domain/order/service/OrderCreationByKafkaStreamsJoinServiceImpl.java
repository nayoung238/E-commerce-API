package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.dto.OrderEvent;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerConfig;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import com.ecommerce.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

/**
 * KStream(주문에 대한 재고 변경 결과) + KTable(waiting 상태의 주문) Join 한 결과(주문 상세)를 DB에 insert 하는 방식 (v2)
 * 주문 생성을 위해 DB 한 번 접근 (insert)
 */
@Service @Primary
@Slf4j
@RequiredArgsConstructor
public class OrderCreationByKafkaStreamsJoinServiceImpl implements OrderCreationService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducerService;
    private final ItemServiceClient itemServiceClient;

    @Override
    @Transactional
    public OrderDto create(OrderDto orderDto) {
        orderDto.initializeOrderEventKey(createOrderEventKey(orderDto.getUserId()));
        orderDto.initializeRequestedAt();
        orderDto.setOrderStatus(OrderStatus.WAITING);
        kafkaProducerService.send(KafkaProducerConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderDto.getOrderEventKey(), OrderEvent.of(orderDto));
        return orderDto;
    }

    @KafkaListener(topics = KStreamKTableJoinConfig.FINAL_ORDER_STREAMS_ONLY_TOPIC)
    public void create(ConsumerRecord<String, OrderEvent> record) {
        log.info("Consuming message success -> Topic: {}, OrderEventKey: {}, OrderStatus: {}",
                record.topic(),
                record.value().getOrderEventKey(),
                record.value().getOrderStatus());

        if(!isExistOrderByOrderEventKey(record.key())) {
            Order order = Order.of(record.value());
            orderRepository.save(order);
            kafkaProducerService.setTombstoneRecord(KafkaProducerConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, record.key());
        }
    }

    @Override
    @KafkaListener(topics = KafkaProducerConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC)
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderEvent> record) {
        if(record.key() != null && record.value() != null) {
            log.info("Consuming message success -> Topic: {}, Key(event Id): {}",
                    record.topic(),
                    record.key());

            try {
                waitBasedOnTimestamp(record.timestamp());

                if (!isExistOrderByOrderEventKey(record.key())) {
                    kafkaProducerService.send(KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC, record.key(), record.value());
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
    @KafkaListener(topics = KafkaProducerConfig.ORDER_PROCESSING_RESULT_REQUEST_STREAMS_ONLY_TOPIC)
    public void requestOrderProcessingResult(ConsumerRecord<String, OrderEvent> record) {
        assert record.key() != null & record.value() != null;
        log.info("Consuming message success -> Topic: {}, OrderEventKey: {}",
                record.topic(),
                record.key());

        // OpenFeign
        OrderStatus orderStatus = itemServiceClient.findOrderProcessingResult(record.key());
        if (orderStatus.equals(OrderStatus.SUCCEEDED) || orderStatus.equals(OrderStatus.FAILED)) {
            if(!isExistOrderByOrderEventKey(record.key())) {
                OrderEvent orderEvent = OrderEvent.of(record.key(), orderStatus);
                kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, record.key(), orderEvent);
            }
        } else if (orderStatus.equals(OrderStatus.SERVER_ERROR)) {
            kafkaProducerService.setTombstoneRecord(KafkaProducerConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, record.key());
        } else {
            resendKafkaMessage(record.key(), record.value());
        }
    }

    private void resendKafkaMessage(String key, OrderEvent orderEvent) {
        String[] redisKey = orderEvent.getRequestedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
        if(isFirstEvent(redisKey[0], orderEvent.getOrderEventKey()))
            kafkaProducerService.send(KafkaProducerConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, key, orderEvent);
        else {
            updateOrderStatusByOrderEventKey(orderEvent.getOrderEventKey(), OrderStatus.FAILED);
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    private boolean isFirstEvent(String key, String orderEventKey) {
        return orderRedisRepository.addOrderEventKey(key, orderEventKey) == 1;
    }

    private void updateOrderStatusByOrderEventKey(String orderEventKey, OrderStatus orderStatus) {
        if(!isExistOrderByOrderEventKey(orderEventKey)) {
            OrderEvent orderEvent = OrderEvent.of(orderEventKey, orderStatus);
            kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, orderEventKey, orderEvent);
        }
    }

    private boolean isExistOrderByOrderEventKey(String orderEventKey) {
        return orderRepository.existsByOrderEventKey(orderEventKey);
    }
}
