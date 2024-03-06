package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.kafka.dto.OrderEvent;
import com.ecommerce.orderservice.kafka.dto.OrderItemEvent;
import com.ecommerce.orderservice.kafka.producer.KafkaProducerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.stream.IntStream;

@SpringBootTest
public class BaseServiceTest {

   @Autowired
    private KafkaProducerService kafkaProducerService;

    public static final Long USER_ID = 2L;
    public static final int ORDER_ITEM_COUNT = 3;

    protected void createOrderProcessingResult(String orderEventKey, OrderStatus orderStatus, final String topic) {
        OrderEvent orderEvent = getOrderProcessingResult(orderEventKey, orderStatus);
        kafkaProducerService.send(topic, orderEventKey, orderEvent);
    }

    private OrderEvent getOrderProcessingResult(String orderEventKey, OrderStatus orderItemStatus) {
        List<OrderItemEvent> orderItemEvent = IntStream.range(0, ORDER_ITEM_COUNT)
                .mapToObj(i -> OrderItemEvent.builder()
                        .itemId((long) i + 1)
                        .orderStatus(orderItemStatus)
                        .build())
                .toList();

        return OrderEvent.builder()
                .userId(USER_ID)
                .orderEventKey(orderEventKey)
                .orderStatus(orderItemStatus)
                .orderItemEvents(orderItemEvent)
                .build();
    }

    protected OrderDto getRequestedOrder() {
        List<OrderItemDto> orderItemDtos = IntStream.range(0, ORDER_ITEM_COUNT)
                .mapToObj(i -> OrderItemDto.builder()
                        .itemId((long) i + 1)
                        .quantity(3L)
                        .build())
                .toList();

        return OrderDto.builder()
                .userId(USER_ID)
                .orderItemDtos(orderItemDtos)
                .build();
    }
}
