package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.BaseServiceTest;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class OrderCreationByDBServiceImplTest extends BaseServiceTest {

    @Autowired
    OrderCreationByDBServiceImpl orderCreationByDBServiceImpl;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @DisplayName("주문 생성 시 주문 상태는 WAITING")
    @Test
    void 요청_주문_생성_테스트() throws InterruptedException {
        // given
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        OrderRequestDto orderRequestDto = getOrderRequestDto(accountId, orderItemIds);

        // when
        OrderDto response = orderCreationByDBServiceImpl.create(orderRequestDto);

        // then
        assertThat(response.getId()).isPositive();
        assertThat(response.getAccountId()).isEqualTo(accountId);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.WAITING);

        List<OrderItemDto> orderItemDtos = response.getOrderItemDtos();
        assertThat(orderItemDtos)
                .hasSize(orderItemDtos.size())
                .allMatch(o -> o.getOrderStatus().equals(OrderStatus.WAITING));

        assertThat(orderItemDtos)
                .extracting(OrderItemDto::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds);
    }
}