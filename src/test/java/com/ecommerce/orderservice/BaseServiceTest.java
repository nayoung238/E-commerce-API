package com.ecommerce.orderservice;

import com.ecommerce.orderservice.domain.order.OrderStatus;
import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

@SpringBootTest
@EmbeddedKafka(
        partitions = 2,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092"
        },
        ports = {9092})
@ActiveProfiles("test")
public class BaseServiceTest {

   @Autowired
    private KafkaProducerService kafkaProducerService;

    protected OrderRequestDto getOrderRequestDto(long accountId, List<Long> orderItemIds) {
        List<OrderItemRequestDto> orderItemRequestDtos = orderItemIds.stream()
                .map(i -> OrderItemRequestDto.builder()
                        .itemId(i)
                        .quantity(3L)
                        .build())
                .toList();

        return OrderRequestDto.builder()
                .accountId(accountId)
                .orderItemRequestDtos(orderItemRequestDtos)
                .build();
    }

    protected OrderKafkaEvent getOrderKafkaEvent(OrderDto orderDto, OrderStatus finalOrderStatus) {
        orderDto.updateOrderStatus(finalOrderStatus);
        orderDto.getOrderItemDtos()
                .forEach(o -> o.updateStatus(finalOrderStatus));
        return OrderKafkaEvent.of(orderDto);
    }
}
