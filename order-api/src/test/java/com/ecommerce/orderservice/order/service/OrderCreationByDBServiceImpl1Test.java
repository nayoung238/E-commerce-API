package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderDto;
import com.ecommerce.orderservice.order.dto.OrderItemDto;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderCreationByDBServiceImpl1Test extends IntegrationTestSupport {

    @Autowired
    OrderCreationByDBServiceImpl orderCreationByDBServiceImpl;

    @MockBean
    InternalEventService internalEventService;

    @MockBean
    KafkaProducerService kafkaProducerService;

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
        final long userId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(userId, orderItemIds);

        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // exercise
        orderCreationByDBServiceImpl.create(orderRequestDto);

        // verify
        verify(internalEventService, times(1))
                .publishInternalEvent(any());

        verify(kafkaProducerService, times(0))
                .send(anyString(), anyString(), any());
    }

    @DisplayName("주문 생성 시 주문 상태는 PROCESSING")
    @Test
    void 요청_주문_생성_테스트() {
        // setup(data)
        final long userId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(userId, orderItemIds);

        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // exercise
        OrderDto response = orderCreationByDBServiceImpl.create(orderRequestDto);

        // verify
        assertThat(response.getOrderProcessingStatus()).isEqualTo(OrderProcessingStatus.PROCESSING);
        List<OrderItemDto> orderItemDtos = response.getOrderItemDtos();
        assertThat(orderItemDtos)
                .hasSize(orderItemDtos.size())
                .allMatch(o -> o.getOrderProcessingStatus().equals(OrderProcessingStatus.PROCESSING));
    }

    @DisplayName("주문 응답에 요청한 주문 아이템이 포함되었는지 확인")
    @Test
    void 요청_주문_목록_테스트() {
        // setup(data)
        final long userId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(userId, orderItemIds);

        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // exercise
        OrderDto response = orderCreationByDBServiceImpl.create(orderRequestDto);

        // verify
        assertThat(response.getId()).isPositive();
        assertThat(response.getUserId()).isEqualTo(userId);
        assertThat(response.getOrderItemDtos())
                .hasSize(orderItemIds.size())
                .extracting(OrderItemDto::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds);
    }
}