package com.ecommerce.orderservice.domain.order.service;

import com.ecommerce.orderservice.domain.order.BaseServiceTest;
import com.ecommerce.orderservice.domain.order.Order;
import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.repository.OrderRepository;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.kafka.config.TopicConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SpringBootTest
@ActiveProfiles("local")
class OrderCreationByKafkaStreamsJoinServiceImplTest extends BaseServiceTest {

    @Autowired
    OrderCreationByKafkaStreamsJoinServiceImpl orderCreationByKafkaStreamsJoinService;

    @Autowired
    OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 최종_주문_생성 () throws InterruptedException {
        //given
        List<OrderDto> orderDtoList = createOrders(1);
        assert orderDtoList.size() == 1;
        assert orderRepository.findAll().isEmpty();
        OrderDto orderDto = orderDtoList.get(0);

        // when
        OrderStatus testStatus = OrderStatus.SUCCEEDED;
        createOrderProcessingResult(orderDto.getOrderEventKey(), testStatus, TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC);
        Thread.sleep(10000);

        // then
        Order finalOrder = orderRepository.findByOrderEventKey(orderDto.getOrderEventKey()).orElse(null);
        assert finalOrder != null;
        Assertions.assertEquals(testStatus, finalOrder.getOrderStatus());
        Assertions.assertTrue(finalOrder.getOrderItems()
                .stream()
                .allMatch(orderItem -> Objects.equals(testStatus, orderItem.getOrderStatus())));
    }

    @Test
    @DisplayName("SUCCEEDED or FAILED 상태만 스트림즈 조인")
    void 스트림즈_조인_필터 () throws InterruptedException {
        //given
        List<OrderDto> orderDtoList = createOrders(1);
        assert orderDtoList.size() == 1;
        assert orderRepository.findAll().isEmpty();
        OrderDto orderDto = orderDtoList.get(0);

        // when
        OrderStatus testStatus = OrderStatus.CANCELED;
        createOrderProcessingResult(orderDto.getOrderEventKey(), testStatus, TopicConfig.ORDER_PROCESSING_RESULT_STREAMS_ONLY_TOPIC);
        Thread.sleep(10000);

        Assertions.assertTrue(orderRepository.findAll().isEmpty());
    }

    private List<OrderDto> createOrders(int n) {
        return IntStream.range(0, n)
                .mapToObj(i -> getRequestedOrder())
                .map(o -> orderCreationByKafkaStreamsJoinService.create(o))
                .collect(Collectors.toList());
    }
}