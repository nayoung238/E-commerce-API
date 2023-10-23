package com.nayoung.orderservice.messagequeue;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.OrderRepository;
import com.nayoung.orderservice.domain.OrderService;
import com.nayoung.orderservice.messagequeue.openFeign.ItemServiceClient;
import com.nayoung.orderservice.messagequeue.openFeign.ItemUpdateLogDto;
import com.nayoung.orderservice.web.dto.OrderDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service @Slf4j
@RequiredArgsConstructor
public class KafkaConsumer {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ItemServiceClient itemServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;
    private final KafkaProducer kafkaProducer;
    private final OrderRedisRepository orderRedisRepository;

    /**
     * 주문에 대한 재고 변경 결과(KStream) + waiting 상태의 주문(KTable)을 Join한 결과(주문 상세)를 DB에 insert
     * 1개의 주문 생성에 대해 DB 한 번 접근 (insert)
     */
    @KafkaListener(topics = KStreamKTableJoinConfig.FINAL_ORDER_CREATION_TOPIC_NAME)
    public void createOrderOnDB(OrderDto orderDto) {
        log.info("Consuming message success -> Topic: {}, Event Id: {}, Order Status: {}",
                KStreamKTableJoinConfig.FINAL_ORDER_CREATION_TOPIC_NAME,
                orderDto.getEventId(),
                orderDto.getOrderStatus());

        orderService.insertFinalOrderOnDB(orderDto);
    }

    /**
     * 주문에 대한 '재고 변경 결과 이벤트'를 바탕으로 상태를 update하는 방식 (v1)
     * 1개의 주문 생성에 대해 DB 두 번 접근 (insert -> update)
     */
    @KafkaListener(topics = KStreamKTableJoinConfig.ITEM_UPDATE_RESULT_TOPIC_NAME)
    public void updateOrderStatus(OrderDto orderDto) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Order Status: {}",
                KStreamKTableJoinConfig.ITEM_UPDATE_RESULT_TOPIC_NAME,
                orderDto.getId(),
                orderDto.getOrderStatus());

        orderService.updateOrderStatusByOrderDto(orderDto);
    }

    /**
     * DB 주문 상태가 확정(succeeded/failed) 되었는지 확인
     * -> 확정되지 않으면 item-service로 결과 직접 요청
     */
    @KafkaListener(topics = {KafkaProducerConfig.TEMPORARY_ORDER_TOPIC_NAME,
                            KafkaProducerConfig.TEMPORARY_RETRY_ORDER_TOPIC_NAME})
    public void checkFinalStatusOfOrder(ConsumerRecord<String, OrderDto> record) {
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                record.topic(),
                record.value().getId(),
                record.value().getEventId());

        try {
            // ProducerRecord 생성 시각 기준으로 5,000ms 이후 -> 주문 상태 확인
            waitBasedOnTimestamp(record.timestamp());

            Optional<Order> order = orderRepository.findByEventId(record.value().getEventId());
            if(order.isPresent()) {
                /*
                    ProducerRecord 생성 시각 기준으로 5,000ms 이후에도 최종 상태 update 되지 않은 경우 (waiting 상태 유지)
                    -> OpenFeign 사용해 item-service로 결과를 직접 요청하기 위한 이벤트 생성
                 */
                if(Objects.equals(OrderItemStatus.WAITING,order.get().getOrderStatus())) {
                    kafkaProducer.send(KafkaProducerConfig.RETRY_REQUEST_ORDER_ITEM_UPDATE_RESULT_TOPIC_NAME, null, record.value());
                }
            }
            // 주문 생성 이벤트가 생성되지 않은 경우
            else {
                kafkaProducer.send(KafkaProducerConfig.TEMPORARY_ORDER_TOPIC_NAME, null, record.value());
            }
        } catch(InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void waitBasedOnTimestamp(long recordTimestamp) throws InterruptedException {
        Instant recordAppendTime = Instant.ofEpochMilli(recordTimestamp);
        Instant now = Instant.now();
        while(now.toEpochMilli() - recordAppendTime.toEpochMilli() < 5000) {
            Thread.sleep(1000);
            now = Instant.now();
        }
    }

    /**
     * OpenFeign 사용해 item-service로 주문에 대한 재고 변경 결과를 직접 요청
     * -> 결과 받으면 주문 상태 update
     * -> 그렇지 않은 경우 첫 이벤트를 기반으로 결과 요청했으면 이벤트 재생성 / 재시도 이벤트 기반이라면 주문 취소
     */
    @KafkaListener(topics = KafkaProducerConfig.RETRY_REQUEST_ORDER_ITEM_UPDATE_RESULT_TOPIC_NAME)
    public void requestItemUpdateResult(OrderDto orderDto) {
        assert orderDto.getEventId() != null;
        log.info("Consuming message success -> Topic: {}, Order Id: {}, Event Id: {}",
                KafkaProducerConfig.RETRY_REQUEST_ORDER_ITEM_UPDATE_RESULT_TOPIC_NAME,
                orderDto.getId(),
                orderDto.getEventId());

        List<ItemUpdateLogDto> itemUpdateLogDtos = getOrderItemUpdateLog(orderDto.getEventId());
        if(!itemUpdateLogDtos.isEmpty()) {
            orderService.updateOrderStatusByItemUpdateLogDtoList(orderDto.getEventId(), itemUpdateLogDtos);
        }
        else {
            String[] redisKey = orderDto.getCreatedAt().toString().split(":");  // key[0] -> order-event:yyyy-mm-dd'T'HH
            // 첫 이벤트를 기반으로 결과 요청했지만, 응답받지 못한 경우 -> 이벤트 재생성 (Retry topic)
            if(orderRedisRepository.addEventId(redisKey[0], orderDto.getEventId()) == 1) {
                kafkaProducer.send(KafkaProducerConfig.TEMPORARY_RETRY_ORDER_TOPIC_NAME, null, orderDto);
            }
            // 재시도 이벤트를 기반으로 결과 요청했지만, 응답받지 못한 경우 -> 주문 실패 처리
            else {
                orderService.updateOrderStatusToFailedByEventId(orderDto.getEventId());
                // TODO: 주문 실패 처리했지만, item-service에서 재고 변경한 경우 -> undo 작업 필요
            }
        }
    }

    /**
     * 30,000ms 동안 최대 4번 결과 요청 (Resilience4j retry 사용)
     * Resilience4j CircuitBreaker 실패율 측정
     * -> Feign Exception 발생 및 30,000ms 이내 응답 여부
     */
    public List<ItemUpdateLogDto> getOrderItemUpdateLog(String eventId) {
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        return circuitBreaker.run(() -> itemServiceClient.getAllOrderItemUpdateResult(eventId), throwable -> new ArrayList<>());
    }
}