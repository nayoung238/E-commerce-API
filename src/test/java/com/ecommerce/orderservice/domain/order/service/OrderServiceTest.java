package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.BaseServiceTest;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderSimpleDto;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.exception.ExceptionCode;
import com.ecommerce.orderservice.exception.OrderException;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("local")
class OrderServiceTest extends BaseServiceTest {

    @Autowired
    OrderService orderService;

    @Autowired
    OrderCreationByDBServiceImpl orderCreationService;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    KafkaProducerService kafkaProducerService;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 주문_리스트_첫_페이지_가져오기 () throws InterruptedException {
        final int count = 2;
        createFinalOrders(count);
        Thread.sleep(10000);

        List<OrderSimpleDto> orderSimpleDtoList = orderService.findOrderByUserIdAndOrderId(USER_ID, null)
                                                                .getOrderSimpleDtoList();
        Assertions.assertTrue(OrderService.PAGE_SIZE >= orderSimpleDtoList.size());
//        Assertions.assertEquals(count, orderDtoList.get(0).getId());
//        Assertions.assertEquals(1, orderDtoList.get(orderDtoList.size() - 1).getId());
    }

    @Test
    void 주문_리스트_특정_커서부터_가져오기 () throws InterruptedException {
        final int count = 10;
        createFinalOrders(count);
        Thread.sleep(10000);

        long orderId = 7L; // cursor
        List<OrderSimpleDto> orderSimpleDtoList = orderService.findOrderByUserIdAndOrderId(USER_ID, orderId)
                                                                .getOrderSimpleDtoList();
        assertEquals(OrderService.PAGE_SIZE, orderSimpleDtoList.size());
//        assertEquals(orderId - 1, orderDtoList.get(0).getId());
//        assertEquals((int) (orderId - OrderService.PAGE_SIZE), orderDtoList.get(orderDtoList.size() - 1).getId());
    }

    private void createFinalOrders(int n) {
        List<OrderDto> orderDtoList = IntStream.range(0, n)
                .mapToObj(i -> getRequestedOrder())
                .map(o -> {
                    o.initializeOrderEventId(createOrderEventId(o.getUserId()));
                    return orderCreationService.create(o);
                })
                .collect(Collectors.toList());

        orderDtoList
                .forEach(orderDto -> createOrderProcessingResult(orderDto.getOrderEventId(), OrderStatus.SUCCEEDED, TopicConfig.ORDER_PROCESSING_RESULT_TOPIC));
    }

    private String createOrderEventId(Long userId) {
        if(userId == null) {
            throw new OrderException(ExceptionCode.NOT_NULL_USER_ID);
        }
        String[] uuid = UUID.randomUUID().toString().split("-");
        return userId + "-" + uuid[0];
    }
}