package com.nayoung.orderservice.domain;

import com.nayoung.orderservice.exception.ExceptionCode;
import com.nayoung.orderservice.exception.OrderException;
import com.nayoung.orderservice.messagequeue.KafkaProducer;
import com.nayoung.orderservice.web.dto.OrderDto;
import com.nayoung.orderservice.web.dto.OrderItemDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final StockRedisRepository stockRedisRepository;
    private final KafkaProducer kafkaProducer;

    public OrderDto create(OrderDto orderDto) {
        List<OrderItemDto> outOfStockItems = orderDto.getOrderItemDtos().stream()
                .filter(r -> !isAvailableToOrder(r.getItemId(), r.getQuantity()))
                .collect(Collectors.toList());

        if(outOfStockItems.isEmpty()) {
            Order order = Order.fromOrderDto(orderDto);
            Order savedOrder = orderRepository.save(order);

            for(OrderItem orderItem : savedOrder.getOrderItems()) {
                OrderItemDto orderItemDto = OrderItemDto.fronmOrderItem(orderItem);
                kafkaProducer.send("update-stock-topic", orderItemDto);
            }
            return OrderDto.fromOrder(savedOrder);
        }
        else {
            return OrderDto.fromFailedOrder(outOfStockItems);
        }
    }

    /**
     * Item-Service: 재고 데이터 write only
     * Order-Service: 재고 데이터 read only
     * 사용자가 장바구니 기능에서 주문을 요청하면 재고를 확인하며,
     * 재고가 없는 주문은 Item-Service로 재고 차감 요청을 보내지 않음
     */
    private boolean isAvailableToOrder(Long itemId, Long quantity) {
        return stockRedisRepository.getItemStock(itemId) >= quantity;
    }

    public OrderDto findOrderByOrderId(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        return OrderDto.fromOrder(order);
    }

    @Transactional
    public void updateOrderStatus(OrderStatus orderStatus, Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderException(ExceptionCode.NOT_FOUND_ORDER));

        order.updateOrderStatus(orderStatus);
        for(OrderItem orderItem : order.getOrderItems())
            orderItem.updateOrderStatus(orderStatus);
    }

    public List<OrderDto> findOrderByCustomerAccountIdAndOrderId(Long customerAccountId, Long cursorOrderId) {
        PageRequest pageRequest = PageRequest.of(0, 5);
        List<Order> orders = new ArrayList<>();
        if(cursorOrderId != null)
            orders = orderRepository.findByCustomerAccountIdAndIdLessThanOrderByIdDesc(customerAccountId, cursorOrderId, pageRequest);
        else
            orders = orderRepository.findByCustomerAccountIdOrderByIdDesc(customerAccountId, pageRequest);

        return orders.stream()
                .sorted(Comparator.comparing(Order::getId).reversed())
                .map(OrderDto::fromOrder)
                .collect(Collectors.toList());
    }
}
