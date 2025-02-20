package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.domain.order.dto.*;
import com.ecommerce.orderservice.order.dto.*;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest
class OrderInquiryServiceTest extends IntegrationTestSupport {

    @Autowired
    OrderInquiryService orderInquiryService;

    @Autowired
    OrderCreationByDBServiceImpl orderCreationService;

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
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);

        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // exercise
        orderCreationService.create(orderRequestDto);

        // verify
        verify(internalEventService, times(1))
                .publishInternalEvent(any());

        verify(kafkaProducerService, times(0))
                .send(anyString(), anyString(), any());
    }

    @DisplayName("최근 주문 조회 시 최근 1건만 조회")
    @Test
    void 최근_주문_조회_테스트() {
        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // setup(data)
        final long accountId = 2L;
        final List<List<Long>> orderItemIds = Stream.of(List.of(1L, 2L, 3L), List.of(4L, 5L, 6L)).toList();
        List<OrderDto> requestedOrders = createOrder(accountId, orderItemIds);
        OrderDto latestRequestedOrder = requestedOrders.stream().max(Comparator.comparing(OrderDto::getId)).get();

        // exercise
        OrderDto latestOrder = orderInquiryService.findLatestOrderByAccountId(accountId);

        // verify
        assertThat(latestOrder).isNotNull();
        assertThat(latestOrder.getId()).isEqualTo(latestRequestedOrder.getId());
        assertThat(latestOrder.getOrderEventId()).isEqualTo(latestRequestedOrder.getOrderEventId());

        assertThat(latestOrder.getOrderItemDtos())
                .hasSize(latestRequestedOrder.getOrderItemDtos().size())
                .extracting(OrderItemDto::getItemId)
                .containsExactlyInAnyOrderElementsOf(latestRequestedOrder.getOrderItemDtos().stream().map(OrderItemDto::getItemId).toList());
    }

    @DisplayName("PageRequest.PageSize보다 주문 수가 적으면 모든 주문 조회")
    @Test
    void 주문_리스트_첫_페이지_조회_테스트1 () {
        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // setup(data)
        final long accountId = 2L;
        final int numberOfRequests = OrderInquiryService.PAGE_SIZE - 2; // 3
        final List<List<Long>> orderItemIds = Stream.generate(() -> List.of(1L, 2L, 3L))
                .limit(numberOfRequests)
                .toList();

        createOrder(accountId, orderItemIds);

        // exercise
        OrderListDto orderListDto = orderInquiryService.findOrderByAccountIdAndOrderId(accountId, null);

        // verify
        final int numberOfOrders = Integer.min(numberOfRequests, OrderInquiryService.PAGE_SIZE); // min(3, 5)
        assertThat(orderListDto.orderSimpleDtoList()).hasSize(numberOfOrders);
    }

    @DisplayName("PageRequest.PageSize보다 주문 수가 많으면 PageRequest.PageSize만큼 조회")
    @Test
    void 주문_리스트_첫_페이지_조회_테스트2 () {
        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // setup(data)
        final long accountId = 2L;
        final int numberOfRequests = OrderInquiryService.PAGE_SIZE + 5; // 10
        final List<List<Long>> orderItemIds = Stream.generate(() -> List.of(1L, 2L, 3L))
                .limit(numberOfRequests)
                .toList();

        createOrder(accountId, orderItemIds);

        // exercise
        OrderListDto orderListDto = orderInquiryService.findOrderByAccountIdAndOrderId(accountId, null);

        // verify
        final int numberOfOrders = Integer.min(numberOfRequests, OrderInquiryService.PAGE_SIZE); // min(10, 5)
        assertThat(orderListDto.orderSimpleDtoList()).hasSize(numberOfOrders);
    }

    @DisplayName("특정 주문 기준으로 주문 목록 요청 시 기준보다 과거에 생성된 주문 목록 조회")
    @Test
    void 주문_리스트_특정_커서부터_가져오기 () {
        // setup(expectations)
        doNothing()
                .when(internalEventService).publishInternalEvent(any());

        // setup(data)
        final long accountId = 2L;
        final int numberOfRequests = OrderInquiryService.PAGE_SIZE + 5; // 10
        final List<List<Long>> orderItemIds = Stream.generate(() -> List.of(1L, 2L, 3L))
                .limit(numberOfRequests)
                .toList();

        List<OrderDto> requestedOrders = createOrder(accountId, orderItemIds);
        requestedOrders = requestedOrders.stream()
                .sorted(Comparator.comparing(OrderDto::getId).reversed())
                .toList();

        final int orderCursorIndex = 1;
        final long orderCursorId = requestedOrders.get(orderCursorIndex).getId();

        final int numberOfOrders = Integer.min(numberOfRequests, OrderInquiryService.PAGE_SIZE); // min(10, 5)
        List<Long> requestedOrderIds = requestedOrders.stream()
                .skip(orderCursorIndex + 1)
                .limit(numberOfOrders)
                .map(OrderDto::getId)
                .toList();

        // exercise
        OrderListDto orderListDto = orderInquiryService.findOrderByAccountIdAndOrderId(accountId, orderCursorId);

        // verify
        assertThat(orderListDto.orderSimpleDtoList())
                .hasSize(numberOfOrders)
                .extracting(OrderSimpleDto::orderId)
                .containsExactlyElementsOf(requestedOrderIds);
    }

    private List<OrderDto> createOrder(long accountId, List<List<Long>> orderItemIds) {
        return orderItemIds.stream()
                .map(i -> {
                    OrderRequestDto requestDto = getOrderRequestDto(accountId, i);
                    return orderCreationService.create(requestDto);
                })
                .toList();
    }
}