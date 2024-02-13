package com.nayoung.orderservice.domain.service;

import com.ecommerce.orderservice.domain.Order;
import com.ecommerce.orderservice.domain.OrderItemStatus;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.domain.service.OrderCreationByKafkaStreamsJoinServiceImpl;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import com.ecommerce.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.ecommerce.orderservice.web.dto.OrderDto;
import com.ecommerce.orderservice.web.dto.OrderItemDto;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@SpringBootTest
@Slf4j
class OrderCreationByKafkaStreamsJoinServiceImplTest {

    @Autowired
    OrderCreationByKafkaStreamsJoinServiceImpl orderCreationService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaProducerService kafkaProducerService;

    private final Long USER_ID = 2L;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 최종_주문_생성 () throws InterruptedException {
        OrderDto requestedOrder = getRequestedOrder();
        OrderDto savedOrderDto = orderCreationService.create(requestedOrder);

        OrderDto orderProcessingResult = getOrderProcessingResult(savedOrderDto.getEventId(), OrderItemStatus.FAILED);
        kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_TOPIC,
                savedOrderDto.getEventId(), orderProcessingResult);

        Thread.sleep(3000);
        Order order = orderRepository.findByEventId(savedOrderDto.getEventId()).orElse(null);
        assert order != null;

        Assertions.assertEquals(OrderItemStatus.FAILED, order.getOrderStatus());
        Assertions.assertTrue(order.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(OrderItemStatus.FAILED, orderItem.getOrderItemStatus())));
    }

    private OrderDto getRequestedOrder() {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .quantity(3L)
                        .build())
                .toList();

        return OrderDto.builder()
                .userId(USER_ID)
                .orderItemDtos(orderItemDtos)
                .build();
    }

    private OrderDto getOrderProcessingResult(String eventId, OrderItemStatus orderItemStatus) {
        List<OrderItemDto> orderItemDtos = Stream
                .iterate(1, i -> i < 3, i -> i + 1)
                .map(i -> OrderItemDto.builder()
                        .itemId((long) i)
                        .orderItemStatus(orderItemStatus)
                        .build())
                .toList();

        return OrderDto.builder()
                .userId(USER_ID)
                .eventId(eventId)
                .orderStatus(orderItemStatus)
                .orderItemDtos(orderItemDtos)
                .build();
    }
}