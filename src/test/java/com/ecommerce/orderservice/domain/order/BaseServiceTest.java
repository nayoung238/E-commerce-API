package com.ecommerce.orderservice.domain.order;

import com.ecommerce.orderservice.domain.order.dto.OrderDto;
import com.ecommerce.orderservice.domain.order.dto.OrderItemDto;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
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

    protected void createOrderProcessingResult(String orderEventId, OrderStatus orderStatus, final String topic) {
        OrderKafkaEvent orderKafkaEvent = getOrderProcessingResult(orderEventId, orderStatus);
        kafkaProducerService.send(topic, orderEventId, orderKafkaEvent);
    }

    private OrderKafkaEvent getOrderProcessingResult(String orderEventId, OrderStatus orderItemStatus) {
        List<OrderItemKafkaEvent> orderItemEvent = IntStream.range(0, ORDER_ITEM_COUNT)
                .mapToObj(i -> OrderItemKafkaEvent.builder()
                        .itemId((long) i + 1)
                        .orderStatus(orderItemStatus)
                        .build())
                .toList();

        return OrderKafkaEvent.builder()
                .userId(USER_ID)
                .orderEventId(orderEventId)
                .orderStatus(orderItemStatus)
                .orderItemKafkaEvents(orderItemEvent)
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
