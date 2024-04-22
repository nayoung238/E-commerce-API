package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.BaseServiceTest;
import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Objects;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("local")
class OrderCreationByDBServiceImplTest extends BaseServiceTest {

    @Autowired
    OrderCreationByDBServiceImpl orderCreationByDBService;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 최종_주문_생성 () throws InterruptedException {
        // given
        createOrders(1);
        Order order = orderRepository.findById(1L).orElse(null);
        assert order != null;

        // when
        OrderStatus testStatus = OrderStatus.SUCCEEDED;
        createOrderProcessingResult(order.getOrderEventId(), testStatus, TopicConfig.ORDER_PROCESSING_RESULT_TOPIC);
        Thread.sleep(10000);

        // then
        Order finalOrder = orderRepository.findByOrderEventId(order.getOrderEventId()).orElse(null);
        assert finalOrder != null;
        Assertions.assertEquals(testStatus, finalOrder.getOrderStatus());
        Assertions.assertTrue(finalOrder.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(testStatus, orderItem.getOrderStatus())));
    }

    private void createOrders(int n) {
        IntStream.range(0, n)
                .mapToObj(i -> getRequestedOrder())
                .forEach(o -> orderCreationByDBService.create(o));
    }
}