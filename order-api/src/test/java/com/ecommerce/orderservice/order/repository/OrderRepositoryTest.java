package com.ecommerce.orderservice.order.repository;

import com.ecommerce.orderservice.IntegrationTestSupport;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.entity.OrderItem;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class OrderRepositoryTest extends IntegrationTestSupport {

    @Autowired
    private OrderRepository orderRepository;

    @AfterEach
    void afterEach() {
        orderRepository.deleteAll();
    }

    @DisplayName("여러 아이템이 담겨있는 주문 1개 생성")
    @Test
    void findOrderByAccountId () {
        // given
        final long accountId = 2L;
        final List<Long> orderItemIds = List.of(2L, 3L, 7L);
        final OrderProcessingStatus orderProcessingStatus = OrderProcessingStatus.SUCCESSFUL;
        Order order = getOrder(accountId, orderItemIds, orderProcessingStatus);

        // when
        orderRepository.save(order);

        // then
        List<Order> orders = orderRepository.findAllByAccountId(accountId);
        assertThat(orders).hasSize(1);

        Order savedOrder = orders.get(0);
        assertThat(savedOrder.getAccountId()).isEqualTo(accountId);
        assertThat(savedOrder.getOrderProcessingStatus()).isEqualTo(orderProcessingStatus);

        assertThat(savedOrder.getOrderItems())
                .hasSize(orderItemIds.size())
                .allMatch(orderItem -> orderItem.getOrderProcessingStatus().equals(orderProcessingStatus))
                .extracting(OrderItem::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds);
    }

    @DisplayName("여러 아이템이 담겨있는 주문 n개 생성")
    @Test
    void findAllOrderByAccountId () {
        // given
        final long accountId = 2L;
        List<Order> orders = new ArrayList<>();
        final List<Long> orderItemIds1 = List.of(2L, 3L, 7L);
        final OrderProcessingStatus orderProcessingStatus1 = OrderProcessingStatus.FAILED;
        orders.add(getOrder(accountId, orderItemIds1, orderProcessingStatus1));

        final List<Long> orderItemIds2 = List.of(5L);
        final OrderProcessingStatus orderProcessingStatus2 = OrderProcessingStatus.SUCCESSFUL;
        orders.add(getOrder(accountId, orderItemIds2, orderProcessingStatus2));

        // when
        orderRepository.saveAll(orders);

        // then
        List<Order> savedOrders = orderRepository.findAllByAccountId(accountId);
        assertThat(savedOrders)
                .hasSize(2)
                .allMatch(order -> order.getAccountId().equals(accountId))
                .extracting(Order::getOrderProcessingStatus)
                .containsExactlyInAnyOrder(orderProcessingStatus1, orderProcessingStatus2);

        Order savedOrder1 = orders.get(0);
        assertThat(savedOrder1.getOrderItems())
                .hasSize(orderItemIds1.size())
                .allMatch(orderItem -> orderItem.getOrderProcessingStatus().equals(orderProcessingStatus1))
                .extracting(OrderItem::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds1);

        Order savedOrder2 = orders.get(1);
        assertThat(savedOrder2.getOrderItems())
                .hasSize(orderItemIds2.size())
                .allMatch(orderItem -> orderItem.getOrderProcessingStatus().equals(orderProcessingStatus2))
                .extracting(OrderItem::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds2);
    }

    @DisplayName("최신 주문 요청 시 생성 날짜가 가장 최근인, id(PK)가 가장 큰 레코드를 1개만 반환")
    @Test
    public void 최신_주문_데이터_조회_테스트() throws InterruptedException {
        // given & when
        final long accountId = 2L;

        final List<Long> orderItemIds1 = List.of(2L, 3L, 7L);
        final OrderProcessingStatus orderProcessingStatus1 = OrderProcessingStatus.FAILED;
        Order request1 = getOrder(accountId, orderItemIds1, orderProcessingStatus1);
        orderRepository.save(request1);

        Thread.sleep(2000);

        final List<Long> orderItemIds2 = List.of(5L);
        final OrderProcessingStatus orderProcessingStatus2 = OrderProcessingStatus.SUCCESSFUL;
        Order request2 = getOrder(accountId, orderItemIds2, orderProcessingStatus2);
        orderRepository.save(request2);

        // then
        Optional<Order> optionalOrder = orderRepository.findLatestOrderByAccountId(accountId);
        assertThat(optionalOrder).isPresent();

        Order savedOrder = optionalOrder.get();
        assertThat(savedOrder.getAccountId()).isEqualTo(accountId);
        assertThat(savedOrder.getOrderProcessingStatus()).isEqualTo(orderProcessingStatus2);

        assertThat(savedOrder.getOrderItems())
                .hasSize(orderItemIds2.size())
                .extracting(OrderItem::getItemId)
                .containsExactlyInAnyOrderElementsOf(orderItemIds2);
    }

    private Order getOrder(long accountId, List<Long> orderItemIds, OrderProcessingStatus orderProcessingStatus) {
        List<OrderItem> orderItems = orderItemIds.stream()
                .map(i -> OrderItem.of(i, 3L, orderProcessingStatus))
                .toList();

        Order order =  Order.of(
                null, // OrderEventId 필드는 KStream-KTable Join 방식에서 사용
                accountId,
                orderItems, orderProcessingStatus,
                null, null);  // requestedAt 필드는 KStream-KTable Join 방식에서 사용하는 필드

        orderItems.forEach(orderItem -> orderItem.initializeOrder(order));
        return order;
    }
}