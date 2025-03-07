package com.ecommerce.orderservice.order.service;

import com.ecommerce.orderservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.orderservice.order.dto.OrderItemRequestDto;
import com.ecommerce.orderservice.order.entity.Order;
import com.ecommerce.orderservice.order.enums.OrderProcessingStatus;
import com.ecommerce.orderservice.order.dto.OrderRequestDto;
import com.ecommerce.orderservice.order.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.orderservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class OrderCreationStreamsServiceImplUnitTest {

    @InjectMocks
    OrderCreationStreamsServiceImpl orderCreationStreamsService;

    @Mock
    OrderRepository orderRepository;

    @Mock
    KafkaProducerService kafkaProducerService;

    @DisplayName("[카프카 호출 테스트] 주문 생성 요청 시 DB 접근하지 않고 카프카 이벤트 발행")
    @Test
    void publish_kafka_event_when_creating_order() {
        // given
        OrderRequestDto orderRequestDto = OrderRequestDto.builder()
            .userId(200L)
            .orderItemRequestDtos(List.of(OrderItemRequestDto.builder().itemId(3L).quantity(21L).build()))
            .build();

        doNothing().when(kafkaProducerService).send(anyString(), anyString(), any(OrderKafkaEvent.class));

        // when
        orderCreationStreamsService.create(orderRequestDto);

        // then
        verify(orderRepository, times(0)).save(any(Order.class));
        verify(kafkaProducerService, times(1))
            .send(anyString(), anyString(), any(OrderKafkaEvent.class));
    }

    @DisplayName("[Tombstone 설정 테스트] 주문 DB insert 후 해당 key를 Tombstone 설정")
    @Test
    void set_tombstone_when_order_db_insert() {
        // given
        OrderKafkaEvent orderKafkaEvent = OrderKafkaEvent.builder()
            .orderEventId("dfs27s9df")
            .userId(24L)
            .orderProcessingStatus(OrderProcessingStatus.SUCCESSFUL)
            .orderItemKafkaEvents(List.of(OrderItemKafkaEvent.builder()
                .itemId(2L).quantity(34L).orderProcessingStatus(OrderProcessingStatus.SUCCESSFUL).build()))
            .build();

        when(orderRepository.save(any(Order.class))).thenReturn(null);
        doNothing().when(kafkaProducerService).setTombstoneRecord(anyString(), anyString());

        // when
        orderCreationStreamsService.insertFinalOrder(orderKafkaEvent);

        // then
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(kafkaProducerService, times(1))
            .setTombstoneRecord(anyString(), anyString());
    }
}