package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
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
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OrderCreationByKafkaStreamsJoinServiceImplTest extends IntegrationTestSupport {

    @Autowired
    OrderCreationByKafkaStreamsJoinServiceImpl orderCreationByKafkaStreamsJoinService;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @Autowired
    OrderInquiryService orderInquiryService;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @DisplayName("KStream-KTable Join된 이벤트의 최종 상태(SUCCESSFUL or FAILED) 설정 후 DB Insert")
    @Test
    void 최종_주문_생성_테스트 () throws InterruptedException {
        // given
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(34L, 12L, 4L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);
        OrderDto requestedOrder = orderCreationByKafkaStreamsJoinService.create(orderRequestDto);

        Thread.sleep(2000);

        // when
        final OrderProcessingStatus finalOrderProcessingStatus = OrderProcessingStatus.SUCCESSFUL;
        sendOrderProcessingResultKafkaEvent(requestedOrder, finalOrderProcessingStatus);

        Thread.sleep(2000);

        // then
        OrderDto finalOrder = orderInquiryService.findLatestOrderByAccountId(accountId);
        assertThat(finalOrder).isNotNull();
        assertThat(finalOrder.getOrderEventId()).isEqualTo(requestedOrder.getOrderEventId());

        assertThat(finalOrder.getOrderProcessingStatus()).isEqualTo(finalOrderProcessingStatus);
        assertThat(finalOrder.getOrderItemDtos())
                .allMatch(orderItemDto -> orderItemDto.getOrderProcessingStatus().equals(finalOrderProcessingStatus));
    }

    @DisplayName("결과 이벤트의 상태가 SUCCESSFUL or FAILED 인 경우에만 스트림즈 조인")
    @Test
    void 스트림즈_조인_필터_테스트 () throws InterruptedException {
        // given
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);
        OrderDto requestedOrder = orderCreationByKafkaStreamsJoinService.create(orderRequestDto);

        Thread.sleep(2000);

        // when
        final OrderProcessingStatus finalOrderProcessingStatus = OrderProcessingStatus.SERVER_ERROR;
        sendOrderProcessingResultKafkaEvent(requestedOrder, finalOrderProcessingStatus);

        Thread.sleep(2000);

        // then
        Assertions.assertThrows(EntityNotFoundException.class,
                () -> orderInquiryService.findLatestOrderByAccountId(accountId));
    }

    /*
        주문에 대한 부수 작업 처리 결과를 이벤트로 발행
     */
    private void sendOrderProcessingResultKafkaEvent(OrderDto orderDto, OrderProcessingStatus orderProcessingStatus) {
        OrderKafkaEvent event = getOrderKafkaEvent(orderDto, orderProcessingStatus);
        kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC, orderDto.getOrderEventId(), event);
    }
}