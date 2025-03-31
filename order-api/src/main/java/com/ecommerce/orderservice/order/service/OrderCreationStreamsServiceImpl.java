package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.dto.request.OrderCreationRequest;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
//import com.ecommerce.orderservice.openfeign.ItemServiceClient;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.order.repository.OrderRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * KStream(주문에 대한 재고 변경 결과) + KTable(waiting 상태의 주문) Join 한 결과(주문 상세)를 DB에 insert
 * 주문 생성을 위해 DB 한 번 접근 (insert)
 */
@Service //@Primary
@Slf4j
@RequiredArgsConstructor
public class OrderCreationStreamsServiceImpl implements OrderCreationService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducerService;
//    private final ItemServiceClient itemServiceClient;

    @Override
    @Transactional
    public OrderDetailResponse create(OrderCreationRequest orderCreationRequest) {
        // DB 영속화하지 않고 Kafka 이벤트 발행하므로 Kafka 이벤트 Key 생성 (스트림 조인에서 사용)
        String orderEventId = getOrderEventId(orderCreationRequest.userId());
        OrderKafkaEvent orderKafkaEvent = OrderKafkaEvent.of(orderCreationRequest, orderEventId);
        kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
        return OrderDetailResponse.of(orderKafkaEvent);
    }

    @Transactional
    public void insertFinalOrder(OrderKafkaEvent orderKafkaEvent) {
        assert orderKafkaEvent.getOrderEventId() != null;

        Order order = Order.of(orderKafkaEvent);
        orderRepository.save(order);

        // Kafka 상태 저장소에서 제거하기 위해 Tombstone 설정
        kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId());
    }

    @Override
    public void checkFinalStatusOfOrder(OrderKafkaEvent orderKafkaEvent, long recordTimestamp) {
        assert orderKafkaEvent.getOrderEventId() != null;
        delayFromTimestamp(recordTimestamp);

        if(!isExistOrderByOrderEventId(orderKafkaEvent.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.ORDER_PROCESSED_RESULT_REQUEST_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
    }

    @Override
    public void requestOrderProcessedResult(OrderKafkaEvent orderKafkaEvent) {
//        assert orderKafkaEvent.getOrderEventId() != null;
//
//        OrderProcessingStatus orderStatus = itemServiceClient.findOrderProcessingResult(orderKafkaEvent.getOrderEventId());
//        if (orderStatus.equals(OrderProcessingStatus.SUCCESSFUL) || orderStatus.equals(OrderProcessingStatus.FAILED)) {
//            if(!isExistOrderByOrderEventId(orderKafkaEvent.getOrderEventId())) {
//                OrderKafkaEvent orderEvent = OrderKafkaEvent.of(orderKafkaEvent.getOrderEventId(), orderStatus);
//                kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId(), orderEvent);
//            }
//        } else if (orderStatus.equals(OrderProcessingStatus.SERVER_ERROR)) {
//            kafkaProducerService.setTombstoneRecord(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId());
//        } else {
//            resendKafkaMessage(orderKafkaEvent);
//        }
    }

    @Override
    public void resendKafkaMessage(OrderKafkaEvent orderKafkaEvent) {
        String redisKey = getRedisKey(orderKafkaEvent.getRequestedAt());
        if(isFirstEvent(redisKey, orderKafkaEvent.getOrderEventId()))
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_STREAMS_ONLY_TOPIC, orderKafkaEvent.getOrderEventId(), orderKafkaEvent);
        else {
            handleOrderFailure(orderKafkaEvent.getOrderEventId());
        }
    }

    @Override
    public boolean isFirstEvent(String redisKey, String orderEventId) {
        return orderRedisRepository.addOrderEventKey(redisKey, orderEventId) == 1;
    }

    @Override
    public void handleOrderFailure(String orderEventId) {
        if(!isExistOrderByOrderEventId(orderEventId)) {
            OrderKafkaEvent orderEvent = OrderKafkaEvent.of(orderEventId, OrderStatus.CANCELED);
            kafkaProducerService.send(TopicConfig.ORDER_PROCESSED_RESULT_STREAMS_ONLY_TOPIC, orderEventId, orderEvent);
        }
    }

    private boolean isExistOrderByOrderEventId(String orderEventId) {
        return orderRepository.existsByOrderEventId(orderEventId);
    }
}
