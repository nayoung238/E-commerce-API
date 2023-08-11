package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@SpringBootTest
public class OrderServiceTest {

    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;
    @Autowired StockRedisRepository stockRedisRepository;

    private final long numberOfItems = 3;
    private final int numberOfOrderItems = 3;
    private final long CUSTOMER_ACCOUNT_ID = 2L;
    private final Long INITIAL_STOCK = 10L;

    @BeforeEach
    void beforeEach() {
        for(long itemId = 0; itemId < numberOfItems; itemId++)
            stockRedisRepository.initialStockQuantity(itemId, INITIAL_STOCK);

        List<OrderItemDto> orderItemRequests = getOrderItemRequests(numberOfOrderItems);
        OrderDto request = OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID)
                .orderItemDtos(orderItemRequests)
                .build();

        for(int i = 0; i < 4; i++) orderService.create(request);
    }

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @Test
    void 재고_충분한_주문_생성 () {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for(long itemId = 0; itemId < numberOfItems; itemId++) {
            orderItemDtos.add(OrderItemDto.builder()
                    .itemId(itemId)
                    .quantity(INITIAL_STOCK - 2).price(1000L)
                    .shopId(1L).build());
        }

        OrderDto request = OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID).orderItemDtos(orderItemDtos).build();
        OrderDto response = orderService.create(request);

        Assertions.assertEquals(orderItemDtos.size(), response.getOrderItemDtos().size());
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderStatus());

        assert(response.getOrderItemDtos().size() != 0);
        Assertions.assertTrue(response.getOrderItemDtos().stream()
                .allMatch(o -> Objects.equals(o.getOrderStatus(), OrderStatus.WAITING)));
    }

    @Test
    void 재고_부족한_주문_생성 () {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for(long itemId = 0; itemId < numberOfItems; itemId++) {
            orderItemDtos.add(OrderItemDto.builder()
                    .itemId(itemId)
                    .quantity(INITIAL_STOCK + 2).price(1000L)
                    .shopId(1L).build());
        }

        OrderDto request = OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID).orderItemDtos(orderItemDtos).build();
        OrderDto response = orderService.create(request);

        Assertions.assertEquals(orderItemDtos.size(), response.getOrderItemDtos().size());
        Assertions.assertEquals(OrderStatus.FAILED, response.getOrderStatus());

        assert(response.getOrderItemDtos().size() != 0);
        Assertions.assertTrue(response.getOrderItemDtos().stream()
                .allMatch(o -> Objects.equals(o.getOrderStatus(), OrderStatus.OUT_OF_STOCK)));
    }

    @Test
    void 일부_상품만_재고가_없는_경우 () {
        List<OrderItemDto> orderItemDtos = new ArrayList<>();
        for(long itemId = 0; itemId < numberOfItems; itemId++) {
            orderItemDtos.add(OrderItemDto.builder()
                    .itemId(itemId)
                    .quantity(INITIAL_STOCK + ((itemId % 2 == 0) ? 1: 0)).price(1000L)
                    .shopId(1L).build());
        }

        OrderDto request = OrderDto.builder()
                .customerAccountId(CUSTOMER_ACCOUNT_ID).orderItemDtos(orderItemDtos).build();
        OrderDto response = orderService.create(request);

        Assertions.assertEquals(OrderStatus.FAILED, response.getOrderStatus());
        assert(response.getOrderItemDtos().size() != 0);
        Assertions.assertTrue(response.getOrderItemDtos().stream()
                .allMatch(o -> Objects.equals(o.getOrderStatus(), OrderStatus.OUT_OF_STOCK)));
    }

    @Test
    void findOrderTest() {
        OrderDto response = orderService.findOrderByOrderId(1L);
        Assertions.assertEquals(numberOfOrderItems, response.getOrderItemDtos().size());
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderStatus());
        assert(response.getOrderItemDtos().size() != 0);
        Assertions.assertEquals(OrderStatus.WAITING, response.getOrderItemDtos().get(0).getOrderStatus());
    }

    @Test
    @DisplayName("cursor Id 존재 유무에 따른 테스트")
    public void cursorBasedOnPaginationTest() {
        List<OrderDto> orderResponses1 = orderService.findOrderByCustomerAccountIdAndOrderId(CUSTOMER_ACCOUNT_ID, null);

        // order entity의 Id는 1부터 차례대로 증가함을 보장
        long count = orderRepository.count();
        List<OrderDto> orderResponses2 = orderService.findOrderByCustomerAccountIdAndOrderId(CUSTOMER_ACCOUNT_ID, count + 1);

        Assertions.assertEquals(orderResponses1.size(), orderResponses2.size());
        assert (orderResponses1.size() > 0);
        Assertions.assertEquals(orderResponses1.get(0).getOrderId(), orderResponses2.get(0).getOrderId());
    }

    private List<OrderItemDto> getOrderItemRequests(int n) {
        List<OrderItemDto> requests = new ArrayList<>();
        for(long i = 0; i < n; i++) {
            requests.add(OrderItemDto.builder()
                    .shopId(i)
                    .itemId(i).quantity(INITIAL_STOCK).price(1200L)
                    .build());
        }
        return requests;
    }

}
