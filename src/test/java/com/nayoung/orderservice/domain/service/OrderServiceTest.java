package com.nayoung.orderservice.domain.service;

import com.nayoung.orderservice.domain.OrderItemStatus;
import com.nayoung.orderservice.domain.repository.OrderRepository;
import com.nayoung.orderservice.kafka.producer.KafkaProducerService;
import com.nayoung.orderservice.kafka.streams.KStreamKTableJoinConfig;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    OrderService orderService;
    @Autowired
    OrderCreationByKafkaStreamsJoinServiceImpl orderCreationService;
    @Autowired
    OrderRepository orderRepository;
    @Autowired
    KafkaProducerService kafkaProducerService;

    private final Long USER_ID = 67L;

    @BeforeEach
    void beforeEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 주문_리스트_첫_페이지_가져오기 () throws InterruptedException {
        createOrders(3);
        Thread.sleep(3000);

        List<OrderDto> orderDtoList = orderService.findOrderByUserIdAndOrderId(USER_ID, null);
        Assertions.assertTrue(OrderService.PAGE_SIZE >= orderDtoList.size());
        Assertions.assertEquals(3L, orderDtoList.getFirst().getId());
        Assertions.assertEquals(1L, orderDtoList.getLast().getId());
    }

    @Test
    void 주문_리스트_특정_커서부터_가져오기 () throws InterruptedException {
        createOrders(8);
        Thread.sleep(3000);

        long orderId = 7L; // cursor
        List<OrderDto> orderDtoList = orderService.findOrderByUserIdAndOrderId(USER_ID, orderId);
        assertEquals(OrderService.PAGE_SIZE, orderDtoList.size());
        assertEquals(orderId - 1, orderDtoList.getFirst().getId());
        assertEquals((int) (orderId - OrderService.PAGE_SIZE), orderDtoList.getLast().getId());
    }

    private void createOrders(int n) {
        List<OrderDto> requestedOrders = IntStream.range(0, n)
                .mapToObj(i -> getRequestedOrder())
                .map(o -> orderCreationService.create(o))
                .collect(Collectors.toList());

        createOrderProcessingResult(requestedOrders);
    }

    private void createOrderProcessingResult(List<OrderDto> requestedOrders) {
        requestedOrders
                .stream()
                .peek(o -> o.updateOrderStatus(OrderItemStatus.SUCCEEDED))
                .forEach(o -> kafkaProducerService.send(KStreamKTableJoinConfig.ORDER_PROCESSING_RESULT_TOPIC, o.getEventId(), o));
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
}