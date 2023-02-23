package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderRequest;
import com.nayoung.orderservice.web.dto.OrderResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrder() {
        OrderRequest request = new OrderRequest();
        request.setAccountId(2L);
        request.setItemId(4L);

        OrderResponse response = orderService.create(request);
        Assertions.assertEquals(request.getAccountId(), response.getAccountId());
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
}
