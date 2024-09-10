package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.UnitTestSupport;
import com.ecommerce.itemservice.domain.item.repository.OrderRedisRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ItemStockServiceBehaviorVerificationTest extends UnitTestSupport {

    @InjectMocks
    ItemStockService itemStockService;

    @Mock
    OrderRedisRepository orderRedisRepository;

    @Mock
    KafkaProducerService kafkaProducerService;

    @Mock
    StockUpdateService stockUpdateService;

    @DisplayName("최초 이벤트는 주문에 속한 모든 아이템에 대한 재고 변경 작업과 결과 이벤트 발행")
    @Test
    void 최초_이벤트_행동_검증 () {
        // setup (data)
        final List<Long> itemIds = List.of(1L, 3L);
        OrderKafkaEvent event = getOrderKafkaEvent(2L, itemIds, 12L, OrderStatus.WAITING);

        // setup(expectations)
        Mockito.when(orderRedisRepository.addEventId(anyString(), anyString()))
                .thenReturn(1L); // Redis 캐싱 성공

        Mockito.when(stockUpdateService.updateStock(any(), any()))
                .thenReturn(getOrderItemKafkaEvent(1L, 1L, OrderStatus.SUCCEEDED));

        // exercise
        itemStockService.updateStock(event, false);

        // verify
        // Redis에서 이벤트 처리 여부 확인 및 이벤트 캐싱
        verify(orderRedisRepository, times(1))
                .addEventId(anyString(), anyString());

        // 주문에 속한 모든 아이템에 대한 재고 변경 작업
        verify(stockUpdateService, times(itemIds.size()))
                .updateStock(any(OrderItemKafkaEvent.class), any());

        // Redis에 이벤트 처리 결과 캐싱
        verify(orderRedisRepository, times(1))
                .setOrderStatus(anyString(), any());

        // 요청에 대한 결과 이벤트 발행
        verify(kafkaProducerService,times(1))
                .sendMessage(anyString(), anyString(), any(OrderKafkaEvent.class));
    }

    @DisplayName("재요청 이벤트는 결과만 반환")
    @Test
    void 재요청_행동_검증 () {
        // setup (data)
        final List<Long> itemIds = List.of(1L, 3L);
        OrderKafkaEvent event = getOrderKafkaEvent(2L, itemIds, 12L, OrderStatus.WAITING);

        // setup(expectations)
        Mockito.when(orderRedisRepository.addEventId(anyString(), anyString()))
                .thenReturn(0L); // Redis 이미 캐싱됨

        // exercise
        itemStockService.updateStock(event, false);

        // 주문에 속한 모든 아이템에 대한 재고 변경 작업
        verify(stockUpdateService, times(0))
                .updateStock(any(OrderItemKafkaEvent.class), any());

        // Redis에서 이벤트 처리 결과 조회
        verify(orderRedisRepository, times(1))
                .getOrderStatus(anyString());
    }
}