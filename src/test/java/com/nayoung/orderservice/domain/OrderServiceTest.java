package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderItemRequest;
import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    private final int numberOfOrderItems = 3;

    @BeforeEach
    void beforeEach() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests();
        OrderRequest request = OrderRequest.builder()
                .customerAccountId(2L)
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
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests();
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
    void getOrdersByAccountId() {
        final int count = 5;
        final long accountId = 3L;

        OrderRequest request = new OrderRequest();
        request.setAccountId(accountId);

        for(int i = 0; i < count; i++) orderService.create(request);

        List<OrderResponse> responseList = orderService.getOrdersByAccountId(accountId);
        Assertions.assertEquals(count, responseList.size());
    }

    private List<OrderItemRequest> getOrderItemRequests() {
        List<OrderItemRequest> requests = new ArrayList<>();
        for(int i = 0; i < numberOfOrderItems; i++) {
            requests.add(OrderItemRequest.builder()
                    .shopId((long) (Math.random() * 12))
                    .itemId((long) (Math.random() * 5)).quantity(34L).price(1200L)
                    .build());
        }
        return requests;
    }

}
