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

        for(int i = 0; i < 4; i++) orderService.create(request);
    }

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void createOrderTest() {
        List<OrderItemRequest> orderItemRequests = getOrderItemRequests(numberOfOrderItems);
        OrderRequest request = OrderRequest.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID + 2)
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
        OrderResponse response = orderService.findOrderByOrderId(new Order.OrderPK(CUSTOMER_ACCOUNT_ID, 1L));
        Assertions.assertEquals(numberOfOrderItems, response.getOrderItemResponses().size());
        Assertions.assertEquals(OrderStatus.ACCEPTED, response.getOrderStatus());
        assert(response.getOrderItemResponses().size() != 0);
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderItemResponses().get(0).getOrderStatus());
    }

    @Test
    @DisplayName("cursor Id 존재 유무에 따른 테스트")
    public void cursorBasedOnPaginationTest() {
        List<OrderResponse> orderResponses1 = orderService.findOrderByCustomerAccountIdAndOrderId(CUSTOMER_ACCOUNT_ID, null);

        // order entity의 Id는 1부터 차례대로 증가함을 보장
        long count = orderRepository.count();
        List<OrderResponse> orderResponses2 = orderService.findOrderByCustomerAccountIdAndOrderId(CUSTOMER_ACCOUNT_ID, count + 1);

        Assertions.assertEquals(orderResponses1.size(), orderResponses2.size());
        assert (orderResponses1.size() > 0);
        Assertions.assertEquals(orderResponses1.get(0).getOrderId(), orderResponses2.get(0).getOrderId());
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
