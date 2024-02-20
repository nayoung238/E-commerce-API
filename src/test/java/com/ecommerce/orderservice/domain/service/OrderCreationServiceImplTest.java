package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.domain.Order;
import com.ecommerce.orderservice.domain.OrderItemStatus;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import com.ecommerce.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.ecommerce.orderservice.web.dto.OrderDto;
import com.ecommerce.orderservice.web.dto.OrderItemDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootTest
class OrderCreationServiceImplTest {

    @Autowired
    OrderCreationServiceImpl orderCreationService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    KafkaProducerService kafkaProducerService;

    private final Long USER_ID = 2L;

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
                .userId(USER_ID)
                .orderItemDtos(orderItemDtos)
                .build();
        orderDto.initializeEventId();

        orderCreationService.create(orderDto);
    }

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 최종_주문_생성 () throws InterruptedException {
        Order order = orderRepository.findById(1L).orElse(null);
        assert order != null;

        OrderItemStatus testStatus = OrderItemStatus.SUCCEEDED;

        OrderDto orderProcessingResult = getOrderProcessingResult(order.getId(), testStatus);
        kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_TOPIC, null, orderProcessingResult);

        Thread.sleep(3000);

        Order finalOrder = orderRepository.findById(order.getId()).orElse(null);
        Assertions.assertEquals(testStatus, Objects.requireNonNull(finalOrder).getOrderStatus());
        Assertions.assertTrue(finalOrder.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(testStatus, orderItem.getOrderItemStatus())));
    }

    private OrderDto getOrderProcessingResult(Long orderId, OrderItemStatus orderItemStatus) {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .orderItemStatus(orderItemStatus)
                        .build())
                .toList();

        return OrderDto.builder()
                .id(orderId)
                .userId(USER_ID)
                .orderStatus(orderItemStatus)
                .orderItemDtos(orderItemDtos)
                .build();
    }
}