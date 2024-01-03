package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.messagequeue.KafkaProducerService;
import com.nayoung.orderservice.messagequeue.KafkaProducerConfig;
import com.nayoung.orderservice.openfeign.ItemServiceClient;
import com.nayoung.orderservice.openfeign.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public abstract class OrderService {

    public final OrderRepository orderRepository;
    private final OrderRedisRepository orderRedisRepository;
    public final KafkaProducerService kafkaProducer;
    private final ItemServiceClient itemServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public abstract OrderDto create(OrderDto orderDto);
    public abstract void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record);
    public abstract void requestOrderItemUpdateResult(ConsumerRecord<String, OrderDto> record);

    /*
        eventId(String) -> KTable & KStream key
        DTO 객체로 이벤트 생성하므로 이벤트 생성 시점에 order ID(PK) 값이 null
        -> customerAccountId 와 randomUUID 조합으로 unique한 값 생성
    */
    public String setEventId(Long customerAccountId) {
        String[] uuid = UUID.randomUUID().toString().split("-");
        return customerAccountId.toString() + "-" + uuid[0];
    }

    /**
     * DB 주문 상태가 확정(succeeded/failed) 되었는지 확인
     * -> 확정되지 않으면 item-service로 결과 직접 요청
     */
    public void waitBasedOnTimestamp(long recordTimestamp) throws InterruptedException {
        Instant recordAppendTime = Instant.ofEpochMilli(recordTimestamp);
        Instant now = Instant.now();
        while(now.toEpochMilli() - recordAppendTime.toEpochMilli() < 5000) {
            Thread.sleep(1000);
            now = Instant.now();
        }
    }

    /**
     * 30,000ms 동안 최대 4번 결과 요청 (Resilience4j retry 사용)
     * Resilience4j CircuitBreaker 실패율 측정
     * -> Feign Exception 발생 및 30,000ms 이내 응답 여부
     */
    public List<ItemUpdateLogDto> getAllOrderItemUpdateResultByEventId(String eventId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        return circuitBreaker.run(() -> itemServiceClient.findAllOrderItemUpdateResultByEventId(eventId), throwable -> new ArrayList<>());
    }

    public void resendKafkaMessage(String key, OrderDto value) {
        String[] redisKey = value.getCreatedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
        if(isFirstEvent(redisKey[0], value.getEventId()))
            kafkaProducer.send(KafkaProducerConfig.TEMPORARY_RETRY_ORDER_TOPIC, key, value);
        else {
            updateOrderStatusToFailedByEventId(value.getEventId());
            // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
        }
    }

    private boolean isFirstEvent(String key, String eventId) {
        return orderRedisRepository.addEventId(key, eventId) == 1;
    }

    private void updateOrderStatusToFailedByEventId(String eventId) {
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
