package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.UnitTestSupport;
import com.ecommerce.itemservice.item.repository.OrderRedisRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.KafkaProducerService;
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
class ItemStockServiceUnitTest extends UnitTestSupport {

    @InjectMocks
    ItemStockService itemStockService;

    @Mock
    OrderRedisRepository orderRedisRepository;

    @Mock
    KafkaProducerService kafkaProducerService;

    @Mock
    StockUpdateService stockUpdateService;

    @DisplayName("[Initial Event 테스트] 최초 이벤트는 주문에 속한 모든 아이템에 대한 재고 변경 작업과 결과 이벤트 발행")
    @Test
    void initial_event_test () {
        // given
        final List<Long> itemIds = List.of(1L, 3L);
        OrderKafkaEvent event = getOrderKafkaEvent(2L, itemIds, 12L, OrderStatus.PROCESSING);

        Mockito.when(orderRedisRepository.addEventId(anyString(), anyString())).thenReturn(1L);
        Mockito.when(stockUpdateService.updateStock(any(), any()))
                .thenReturn(getOrderItemKafkaEvent(1L, 1L, OrderStatus.SUCCESSFUL));

        // when
        itemStockService.updateStock(event, false);

        // verify
        // 레디스에 이벤트 처리 여부 확인 및 이벤트 캐싱
        verify(orderRedisRepository, times(1)).addEventId(anyString(), anyString());

        // 주문에 속한 모든 아이템에 대한 재고 변경 작업
        verify(stockUpdateService, times(itemIds.size())).updateStock(any(OrderItemKafkaEvent.class), any());

        // 레디스에 이벤트 처리 결과 캐싱
        verify(orderRedisRepository, times(1)).setOrderProcessingStatus(anyString(), any());

        // 요청에 대한 결과 이벤트 발행
        verify(kafkaProducerService,times(1)).sendMessage(anyString(), anyString(), any(OrderKafkaEvent.class));
    }

    @DisplayName("[Retry Event 테스트] 재요청 이벤트는 결과만 반환")
    @Test
    void retry_event_test () {
        // given
        final List<Long> itemIds = List.of(1L, 3L);
        OrderKafkaEvent event = getOrderKafkaEvent(2L, itemIds, 12L, OrderStatus.PROCESSING);

        Mockito.when(orderRedisRepository.addEventId(anyString(), anyString()))
                .thenReturn(0L); // Redis 이미 캐싱됨

        // when
        itemStockService.updateStock(event, false);

        // then
        // 주문에 속한 모든 아이템에 대한 재고 변경 작업
        verify(stockUpdateService, times(0)).updateStock(any(OrderItemKafkaEvent.class), any());

        // 레디스에서 이벤트 처리 결과 조회
        verify(orderRedisRepository, times(1)).getOrderProcessingStatus(anyString());
    }
}