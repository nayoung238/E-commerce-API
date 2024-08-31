package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

public interface OrderCreationService {

    OrderDto create(OrderRequestDto orderRequestDto);
    void checkFinalStatusOfOrder(OrderKafkaEvent orderKafkaEvent, long recordTimestamp);
    void requestOrderProcessingResult(OrderKafkaEvent orderKafkaEvent);
    void resendKafkaMessage(OrderKafkaEvent orderKafkaEvent);
    boolean isFirstEvent(String redisKey, String orderEventId);
    void updateOrderStatus(String orderEventId, OrderStatus orderStatus);

    default String getOrderEventId(@NotNull(message = "사용자 아이디는 필수입니다.")
                                   @Positive(message = "사용자 아이디는 1 이상이어야 합니다.") long accountId) {
        String[] uuid = UUID.randomUUID().toString().split("-");
        return accountId + "-" + uuid[0];
    }

    /*
        Kafka Record의 Timestamp 기준으로 5000ms 후
        최종 주문이 생성되었는지 확인
     */
    default void delayFromTimestamp(long recordTimestamp) {
        try {
            Instant recordAppendTime = Instant.ofEpochMilli(recordTimestamp);
            Instant targetTime = recordAppendTime.plusMillis(5000);
            Duration duration = Duration.between(Instant.now(), targetTime);

            if (!duration.isNegative())
                Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    default String getRedisKey(@NotNull(message = "requestedAt 값은 필수입니다.") LocalDateTime requestedAt) {
        String[] redisKey = requestedAt.toString().split(":");
        return redisKey[0];  // order-event:yyyy-mm-dd'T'HH
    }
}
