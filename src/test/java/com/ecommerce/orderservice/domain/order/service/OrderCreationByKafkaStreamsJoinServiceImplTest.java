package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderCreationByKafkaStreamsJoinServiceImplTest extends IntegrationTestSupport {

    @Autowired
    OrderCreationByKafkaStreamsJoinServiceImpl orderCreationByKafkaStreamsJoinService;

    @Autowired
    OrderInquiryService orderInquiryService;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @DisplayName("KStream-KTable Join된 이벤트만 최종 상태(SUCCEEDED or FAILED) 설정 후 DB Insert")
    @Test
    void 최종_주문_생성 () throws InterruptedException {
        // given & when
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(34L, 12L, 4L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);
        OrderDto requestedOrder = orderCreationByKafkaStreamsJoinService.create(orderRequestDto);

        Thread.sleep(2000);

        final OrderStatus finalOrderStatus = OrderStatus.SUCCEEDED;
        OrderKafkaEvent event = getOrderKafkaEvent(requestedOrder, finalOrderStatus);
        kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, requestedOrder.getOrderEventId(), event);

        Thread.sleep(5000);

        // then
        OrderDto finalOrder = orderInquiryService.findLatestOrderByAccountId(accountId);
        assertThat(finalOrder).isNotNull();
        assertThat(finalOrder.getOrderEventId()).isEqualTo(requestedOrder.getOrderEventId());
        assertThat(finalOrder.getOrderItemDtos())
                .hasSize(orderItemIds.size())
                .extracting(OrderItemDto::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds);

        assertThat(finalOrder.getOrderStatus()).isEqualTo(finalOrderStatus);
        assertThat(finalOrder.getOrderItemDtos())
                .allMatch(orderItemDto -> orderItemDto.getStatus().equals(finalOrderStatus));
    }

    @DisplayName("결과 이벤트의 상태가 SUCCEEDED or FAILED 인 경우에만 스트림즈 조인")
    @Test
    void 스트림즈_조인_필터 () throws InterruptedException {
        // given & when
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);
        OrderDto requestedOrder = orderCreationByKafkaStreamsJoinService.create(orderRequestDto);

        Thread.sleep(2000);

        final OrderStatus finalOrderStatus = OrderStatus.SERVER_ERROR;
        OrderKafkaEvent event = getOrderKafkaEvent(requestedOrder, finalOrderStatus);
        kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, requestedOrder.getOrderEventId(), event);

        Thread.sleep(5000);

        // then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> orderInquiryService.findLatestOrderByAccountId(accountId));
    }
}