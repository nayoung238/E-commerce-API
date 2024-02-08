package com.nayoung.orderservice.domain.service;

import com.nayoung.orderservice.domain.Order;
import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.repository.OrderRepository;
import com.nayoung.orderservice.kafka.producer.KafkaProducerService;
import com.nayoung.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootTest
class OrderCreationServiceImplTest {

    @Autowired
    OrderCreationServiceImpl orderService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    KafkaProducerService kafkaProducerService;

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
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItemDtos(orderItemDtos)
                .build();
        orderDto.initializeEventId();

        orderService.create(orderDto);
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
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderStatus(orderItemStatus)
                .orderItemDtos(orderItemDtos)
                .build();
    }
}