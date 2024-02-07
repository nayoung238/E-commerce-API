package com.nayoung.orderservice.domain.service;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.repository.OrderRepository;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceV1Test {

    @Autowired
    OrderServiceV1 orderServiceV1;
    @Autowired
    OrderRepository orderRepository;

    private final Long CUSTOMER_ACCOUNT_ID = 2L;

    @BeforeEach
    void beforeEach() {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .quantity(3L)
                        .build())
                .toList();

        OrderDto orderDto = OrderDto.builder()
                .eventId(orderServiceV1.setEventId(CUSTOMER_ACCOUNT_ID))
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItemDtos(orderItemDtos)
                .build();

        orderServiceV1.create(orderDto);
    }

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 주문_상턔_변경 () {
        Order order = orderRepository.findById(1L).orElse(null);
        assert order != null;

        orderServiceV1.updateOrderStatusByEventId(order.getEventId(), OrderItemStatus.FAILED);

        Order savedOrder = orderRepository.findById(1L).orElse(null);
        Assertions.assertEquals(OrderItemStatus.FAILED, Objects.requireNonNull(savedOrder).getOrderStatus());
        Assertions.assertTrue(savedOrder.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(OrderItemStatus.FAILED, orderItem.getOrderItemStatus())));
    }
}