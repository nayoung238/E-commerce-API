package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderItemRequest;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    private final int numberOfOrderItems = 3;
    private final long CUSTOMER_ACCOUNT_ID = 2L;

    @BeforeEach
    void beforeEach() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests(numberOfOrderItems);
        OrderRequest request = OrderRequest.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItems(orderItemRequests)
                .build();

        orderService.create(request);
    }

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrderTest() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests(numberOfOrderItems);
        OrderRequest request = OrderRequest.builder()
                .customerAccountId(3L)
                .orderItems(orderItemRequests)
                .build();

        OrderResponse response = orderService.create(request);
        Assertions.assertEquals(orderItemRequests.size(), response.getOrderItemResponses().size());
        Assertions.assertEquals(OrderStatus.ACCEPTED, response.getOrderStatus());
        assert(response.getOrderItemResponses().size() != 0);
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderItemResponses().get(0).getOrderStatus());
    }

    @Test
    void findOrderTest() {
        OrderResponse response = orderService.findOrderByOrderId(1L);
        Assertions.assertEquals(numberOfOrderItems, response.getOrderItemResponses().size());
        Assertions.assertEquals(OrderStatus.ACCEPTED, response.getOrderStatus());
        assert(response.getOrderItemResponses().size() != 0);
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderItemResponses().get(0).getOrderStatus());
    }

    @Test
    @DisplayName("최대 5개의 주문만 가져오는 테스트")
    void findOrderByCustomerAccountIdTest() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests(numberOfOrderItems);
        OrderRequest request = OrderRequest.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItems(orderItemRequests)
                .build();
        orderService.create(request);
        orderService.create(request);

        List<OrderResponse> responses = orderService.findOrderByCustomerAccountId(CUSTOMER_ACCOUNT_ID);
        Assertions.assertEquals(3L, responses.size());

        for(int i = 0; i < 10; i++)
            orderService.create(request);
        responses = orderService.findOrderByCustomerAccountId(CUSTOMER_ACCOUNT_ID);
        Assertions.assertEquals(5L, responses.size());
    }

    private List<OrderItemRequest> getOrderItemRequests(int n) {
        List<OrderItemRequest> requests = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            requests.add(OrderItemRequest.builder()
                    .shopId((long) (Math.random() * 12))
                    .itemId((long) (Math.random() * 5)).quantity(34L).price(1200L)
                    .build());
        }
        return requests;
    }

}
