package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.IntegrationTestSupport;
import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class StockUpdateByKafkaStreamsServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private StockUpdateByKafkaStreamsServiceImpl stockUpdateByKafkaStreamsService;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRedisRepository itemRedisRepository;

    @MockBean
    private KafkaProducerService kafkaProducerService;

    @BeforeEach
    void afterEach() {
        itemRepository.deleteAll();
    }

    @DisplayName("Redis에서 실시간으로 아이템 재고 소비")
    @Test
    void 재고_소비_테스트() {
        // setup(data)
        final long INITIAL_STOCK = 100L;
        Item item = getItem("TEST_ITEM_NAME", INITIAL_STOCK, 1000L);
        saveItem(item);

        doNothing()
                .when(kafkaProducerService)
                .sendMessage(anyString(), anyString(), anyLong());

        final long REQUESTED_QUANTITY = 10L;
        OrderItemKafkaEvent orderItemKafkaEvent = getOrderItemKafkaEvent(item.getId(), REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING);

        // exercise
        stockUpdateByKafkaStreamsService.updateStock(orderItemKafkaEvent, ItemProcessingStatus.STOCK_CONSUMPTION);

        // verify
        // Redis에서 실시간으로 재고 변경 작업이 반영되어야 함
        final Long expectedStock = INITIAL_STOCK - REQUESTED_QUANTITY;
        long stockInRedis = itemRedisRepository.findItemStock(item.getId());
        assertThat(stockInRedis).isEqualTo(expectedStock);
    }

    @DisplayName("Kafka Streams Join을 위해 이벤트 발행")
    @Test
    void 카프카_스트림즈_조인_이벤트_발행_테스트 () {
        // setup(data)
        final long INITIAL_STOCK = 100L;
        final int NUMBER_OF_ITEMS = 3;
        List<Item> items = IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .mapToObj(i -> getItem("test_item_" + i, INITIAL_STOCK, 1000))
                .toList();

        items.forEach(this::saveItem);

        final long REQUESTED_QUANTITY = 10L;
        List<OrderItemKafkaEvent> orderItemKafkaEvents = items.stream()
                .map(i -> getOrderItemKafkaEvent(i.getId(), REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING))
                .toList();

        // setup(expectations)
        doNothing()
                .when(kafkaProducerService)
                .sendMessage(anyString(), anyString(), anyLong());

        // exercise
        orderItemKafkaEvents
                        .forEach(event -> stockUpdateByKafkaStreamsService.updateStock(event, ItemProcessingStatus.STOCK_CONSUMPTION));

        // verify
        verify(kafkaProducerService, times(NUMBER_OF_ITEMS))
                .sendMessage(anyString(), anyString(), anyLong());
    }

    private void saveItem(Item item) {
        itemRepository.save(item);
        itemRedisRepository.initializeItemStock(item.getId(), item.getStock());
    }
}