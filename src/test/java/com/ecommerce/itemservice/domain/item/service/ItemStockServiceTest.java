package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.IntegrationTestSupport;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.mockito.Mockito.*;

class ItemStockServiceTest extends IntegrationTestSupport {

    @InjectMocks
    @Autowired
    ItemStockService itemStockService;

    @Autowired
    ItemRepository itemRepository;

    @Mock
    KafkaProducerService kafkaProducerService;

    private static final int NUMBER_OF_ITEMS = 3;
    private static final Long INITIAL_STOCK = 100L;
    private static final List<Long> itemIds = new ArrayList<>();

    @BeforeEach
    void beforeEach() {
        IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .forEach(i -> {
                    Item item = getItem("test_item_name", INITIAL_STOCK, 1000);
                    itemRepository.save(item);
                    itemIds.add(item.getId());
                });
    }

    @DisplayName("테스트 환경에서 카프카 이벤트가 발행되지 않도록 설정")
    @Test
    void kafkaProducerServiceTest() {
        final long accountId = 1L;
        assert itemIds.size() == 3;
        final List<Long> requestedItemIds = List.of(itemIds.get(0), itemIds.get(2));
        final long quantity = 5L;
        OrderKafkaEvent request = getOrderKafkaEvent(accountId, itemIds, quantity, OrderStatus.WAITING);

        itemStockService.updateStock(request, false);

        // 카프카 이벤트 발행 차단 검증
        verify(kafkaProducerService, times(0))
                .sendMessage(anyString(), anyString(), any(OrderKafkaEvent.class));
    }
}