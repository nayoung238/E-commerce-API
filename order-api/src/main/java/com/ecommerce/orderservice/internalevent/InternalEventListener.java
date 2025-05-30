package com.ecommerce.orderservice.internalevent;

import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.kafka.dto.updatedEvent.OrderUpdatedEvent;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.order.enums.OrderStatus;
import com.ecommerce.orderservice.order.service.OrderQueryService;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@RequiredArgsConstructor
@Component
@Slf4j
public class InternalEventListener {

    private final InternalEventService internalEventService;
    private final KafkaProducerService kafkaProducerService;
    private final OrderQueryService orderQueryService;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void insertOrderCreationInternalEvent(OrderInternalEvent event) {
        internalEventService.saveOrderCreationInternalEvent(event);
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void createKafkaEvent(OrderInternalEvent orderInternalEvent) {
        if (orderInternalEvent.getOrderStatus().equals(OrderStatus.CREATION)) {
            OrderDetailResponse orderDetailResponse = orderQueryService.findOrderByOrderEventId(orderInternalEvent.getOrderEventId());
            kafkaProducerService.send(TopicConfig.REQUESTED_ORDER_TOPIC, orderDetailResponse.getOrderEventId(), OrderKafkaEvent.of(orderDetailResponse));
        }

        // API composer 서비스에서 MyPage 데이터 변경 or 삭제하는 이벤트
        kafkaProducerService.send(TopicConfig.ORDER_UPDATED_TOPIC, OrderUpdatedEvent.of(orderInternalEvent));
    }
}
