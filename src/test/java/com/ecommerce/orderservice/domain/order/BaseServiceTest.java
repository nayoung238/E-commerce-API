package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.domain.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
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
}
