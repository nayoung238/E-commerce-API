package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.KafkaProducerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockUpdateByKafkaStreamsServiceUnitTest {

    @InjectMocks
    private StockUpdateByKafkaStreamsServiceImpl stockUpdateByKafkaStreamsService;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemRedisRepository itemRedisRepository;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @DisplayName("[카프카 이벤트 발행 테스트] 레디스에서 차감 성공 시 카프카 이벤트 발행")
    @Test
    void kafka_event_publish_test() {
        // given
        final long INITIAL_STOCK = 100L;
        Item item = Item.builder()
            .id(1L)
            .name("test-name-56")
            .stock(INITIAL_STOCK)
            .price(2000L)
            .build();

        when(itemRepository.findById(anyLong())).thenReturn(Optional.of(item));
        when(itemRedisRepository.decrementItemStock(anyLong(), anyLong())).thenReturn(3L);
        doNothing().when(kafkaProducerService).sendMessage(anyString(), anyString(), anyLong());

        final long REQUESTED_QUANTITY = 10L;
        OrderItemKafkaEvent orderItemKafkaEvent = OrderItemKafkaEvent.of(1L, REQUESTED_QUANTITY, OrderStatus.PROCESSING);

        // when
        stockUpdateByKafkaStreamsService.updateStock(orderItemKafkaEvent, ItemProcessingStatus.STOCK_CONSUMPTION);

        // then
        verify(itemRedisRepository, times(1)).decrementItemStock(anyLong(), anyLong());
        verify(kafkaProducerService, times(1)).sendMessage(anyString(), anyString(), anyLong());
    }
}