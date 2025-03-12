package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.common.exception.CustomException;
import com.ecommerce.orderservice.common.exception.ErrorCode;
import com.ecommerce.orderservice.order.dto.response.OrderItemResponse;
import com.ecommerce.orderservice.order.dto.response.OrderDetailResponse;
import com.ecommerce.orderservice.order.dto.response.OrderSummaryResponse;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OrderQueryServiceTest extends IntegrationTestSupport {

    @Autowired
    OrderQueryService orderQueryService;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @DisplayName("[주문 단건 조회 성공 테스트] 주문 단건 조회 시 userId가 일치하면 응답")
    @Test
    void findOrderById_whenUseridMatches() {
        // given
        final long userId = 1L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        Order requestedOrder = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
        requestedOrder = orderRepository.save(requestedOrder);

        // when
        OrderDetailResponse retrievedOrder = orderQueryService.findOrderById(requestedOrder.getId(), userId);

        // then
        assertThat(retrievedOrder).isNotNull();
        assertThat(retrievedOrder.getId()).isEqualTo(requestedOrder.getId());
        assertThat(retrievedOrder.getUserId()).isEqualTo(requestedOrder.getUserId());
        assertThat(retrievedOrder.getOrderItems())
            .hasSize(orderItemIds.size())
            .extracting(OrderItemResponse::getItemId)
            .containsExactlyInAnyOrderElementsOf(orderItemIds);
    }

    @DisplayName("[주문 단건 조회 실패 테스트] 주문 단건 조회 시 userId가 일치하지 않으면 FORBIDDEN 예외")
    @Test
    void findOrderFailedTest_whenInvalidUserId() {
        // given
        final long userId = 2L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        Order requestedOrder = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
        requestedOrder = orderRepository.save(requestedOrder);

        // when & then
        final Order finalRequestedOrder = requestedOrder;
        assertThatThrownBy(() -> orderQueryService.findOrderById(finalRequestedOrder.getId(), userId + 1))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customException = (CustomException) ex;
                assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.FORBIDDEN);
                assertThat(customException.getErrorCode().getMessage()).isEqualTo(ErrorCode.FORBIDDEN.getMessage());
            });
    }

    @DisplayName("[주문 목록 조회 성공 테스트] PageRequest.PageSize 보다 주문 수가 많으면 PageSize 만큼만 주문 조회")
    @Test
    void shouldNotExceedPageSizeTest() {
        // given
        final long userId = 3L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        for (int i = 0; i < OrderQueryService.PAGE_SIZE + 2; i++) {
            Order order = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
            orderRepository.save(order);
        }

        // when
        final Long orderCursorId = null;
        List<OrderSummaryResponse> orders = orderQueryService.findOrdersByUserIdAndOrderId(userId, orderCursorId);

        // then
        assertThat(orders).isNotNull();
        assertThat(orders).hasSize(OrderQueryService.PAGE_SIZE);
    }

    @DisplayName("[주문 목록 조회 성공 테스트] PageRequest.PageSize 보다 주문 수가 적으면 모든 주문 조회")
    @Test
    void shouldReturnAllOrders_WhenOrderCountIsLessThanPageSize() {
        // given
        final long userId = 4L;
        final List<Long> orderItemIds = List.of(2L, 3L);
        for (int i = 0; i < OrderQueryService.PAGE_SIZE - 2; i++) {
            Order order = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
            orderRepository.save(order);
        }

        // when
        final Long orderCursorId = null;
        List<OrderSummaryResponse> orders = orderQueryService.findOrdersByUserIdAndOrderId(userId, orderCursorId);

        // then
        assertThat(orders).isNotNull();
        assertThat(orders).hasSizeLessThanOrEqualTo(OrderQueryService.PAGE_SIZE);
    }

    @DisplayName("[주문 목록 조회 성공 테스트] 주문 커서 ID가 null이면 최신 주문 조회")
    @Test
    void order_cursor_id_null_test() {
        // given
        final long userId = 5L;
        final List<Long> orderItemIds = List.of(1L, 2L, 3L);
        final List<Order> requestedOrder = new ArrayList<>();
        for (int i = 0; i < OrderQueryService.PAGE_SIZE; i++) {
            Order order = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
            orderRepository.save(order);
            requestedOrder.add(order);
        }

        // when
        final Long orderCursorId = null;
        List<OrderSummaryResponse> orders = orderQueryService.findOrdersByUserIdAndOrderId(userId, orderCursorId);

        // then
        assertThat(orders).isNotNull();
        assertThat(orders.get(0).orderId()).isEqualTo(requestedOrder.get(requestedOrder.size() - 1).getId());
    }

    @DisplayName("[주문 목록 조회 성공 테스트] 커서 값에 맞춰 주문 목록을 역순으로 조회")
    @Test
    void shouldReturnOrdersInReverseOrder_WhenCursorIsProvided() {
        // given
        final long userId = 6L;
        final List<Long> orderItemIds = List.of(1L, 2L);
        final List<Order> requestedOrder = new ArrayList<>();
        for (int i = 0; i < OrderQueryService.PAGE_SIZE + 4; i++) {
            Order order = getOrder(userId, orderItemIds, OrderProcessingStatus.SUCCESSFUL);
            orderRepository.save(order);
            requestedOrder.add(order);
        }

        // when
        final int index = 4;
        final Long orderCursorId = requestedOrder.get(index).getId();
        List<OrderSummaryResponse> orders = orderQueryService.findOrdersByUserIdAndOrderId(userId, orderCursorId);

        // then
        assertThat(orders).isNotNull();
        assertThat(orders).hasSize(index);
        assertThat(orders.get(0).orderId()).isEqualTo(requestedOrder.get(index - 1).getId());
    }
}