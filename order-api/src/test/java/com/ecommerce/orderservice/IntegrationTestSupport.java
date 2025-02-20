package com.ecommerce.orderservice;

import com.ecommerce.orderservice.domain.order.OrderProcessingStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@EmbeddedKafka(
        partitions = 2,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092"
        },
        ports = {9092})
@ActiveProfiles("test")
public class IntegrationTestSupport {

    protected OrderRequestDto getOrderRequestDto(long accountId, List<Long> orderItemIds) {
        List<OrderItemRequestDto> orderItemRequestDtos = orderItemIds.stream()
                .map(i -> OrderItemRequestDto.builder()
                    .itemId(i)
                    .quantity(3L)
                    .build())
                .toList();

        return OrderRequestDto.of(accountId, orderItemRequestDtos);
    }

    protected OrderKafkaEvent getOrderKafkaEvent(OrderDto orderDto, OrderProcessingStatus finalOrderProcessingStatus) {
        orderDto.updateOrderStatus(finalOrderProcessingStatus);
        orderDto.getOrderItemDtos()
                .forEach(o -> o.updateStatus(finalOrderProcessingStatus));
        return OrderKafkaEvent.of(orderDto);
    }
}
