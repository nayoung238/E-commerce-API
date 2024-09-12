package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderCreationByDBServiceImpl2Test extends IntegrationTestSupport {

    @Autowired
    OrderCreationByDBServiceImpl orderCreationByDBServiceImpl;

    @MockBean
    InternalEventService internalEventService;

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

    @DisplayName("테스트 환경에서 주문 생성 이벤트를 위한 Application Event 발행을 차단함")
    @Test
    void 내부_이벤트_발행_여부_테스트() {
        // setup(data)
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);

        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // exercise
        orderCreationByDBServiceImpl.create(orderRequestDto);

        // verify
        verify(internalEventService, times(1)).publishInternalEvent(any());
    }

    @DisplayName("주문에 대한 처리 결과 이벤트 수신 후 최종 상태(SUCCESSFUL or FAILED)로 update")
    @Test
    void 최종_주문_상태_테스트() throws InterruptedException {
        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // setup(data) & exercise
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);
        OrderDto waitingOrder = orderCreationByDBServiceImpl.create(orderRequestDto);

        Thread.sleep(2000);

        final OrderProcessingStatus finalOrderProcessingStatus = OrderProcessingStatus.SUCCESSFUL;
        sendOrderProcessingResultKafkaEvent(waitingOrder, finalOrderProcessingStatus);

        Thread.sleep(3000);

        // verify
        OrderDto finalOrder = orderInquiryService.findLatestOrderByAccountId(accountId);
        assertThat(finalOrder).isNotNull();
        assertThat(finalOrder.getOrderEventId()).isEqualTo(waitingOrder.getOrderEventId());

        assertThat(finalOrder.getOrderProcessingStatus()).isEqualTo(finalOrderProcessingStatus);
        assertThat(finalOrder.getOrderItemDtos())
                .allMatch(orderItemDto -> orderItemDto.getOrderProcessingStatus().equals(finalOrderProcessingStatus));
    }

    /*
        주문에 대한 부수 작업 처리 결과를 이벤트로 발행
     */
    private void sendOrderProcessingResultKafkaEvent(OrderDto orderDto, OrderProcessingStatus orderProcessingStatus) {
        OrderKafkaEvent event = getOrderKafkaEvent(orderDto, orderProcessingStatus);
        kafkaProducerService.send(TopicConfig.ORDER_PROCESSING_RESULT_TOPIC, orderDto.getOrderEventId(), event);
    }
}