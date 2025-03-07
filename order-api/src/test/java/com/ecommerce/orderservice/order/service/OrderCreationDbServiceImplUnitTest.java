package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.internalevent.entity.OrderInternalEvent;
import com.ecommerce.orderservice.internalevent.service.InternalEventService;
import com.ecommerce.orderservice.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderCreationDbServiceImplUnitTest {

    @InjectMocks
    OrderCreationDbServiceImpl orderCreationDbServiceImpl;

    @Mock
    OrderRepository orderRepository;

    @Mock
    InternalEventService internalEventService;

    @DisplayName("[내부 이벤트 생성 테스트] 주문 생성 시 내부 이벤트 발행")
    @Test
    void internalEventPublishTest() {
        // given
        final long userId = 4L;
        final List<Long> orderItemIds = List.of(2L, 3L);
        List<OrderItemRequestDto> orderItemRequestDtos = orderItemIds.stream()
            .map(id -> OrderItemRequestDto.builder()
                .itemId(id)
                .quantity(3L)
                .build())
            .toList();

        OrderRequestDto orderRequestDto = OrderRequestDto.builder()
            .userId(userId)
            .orderItemRequestDtos(orderItemRequestDtos)
            .build();

        Order mockOrder = Order.of(orderRequestDto);
        mockOrder.initializeOrderEventId("s7dlf234sjr2");

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);

        // when
        orderCreationDbServiceImpl.create(orderRequestDto);

        // verify
        verify(orderRepository, times(1))
            .save(any(Order.class));

        verify(internalEventService, times(1))
                .publishInternalEvent(any(OrderInternalEvent.class));
    }
}