package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.IntegrationTestSupport;
import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class StockUpdateByRedissonServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private StockUpdateByRedissonServiceImpl stockUpdateByRedissonServiceImpl;

    @Autowired
    private ItemRepository itemRepository;

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

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        itemIds.clear();
    }

    @DisplayName("아이템 재고보다 적은 수량으로 차감하면 DB에 값이 반영되어야 함")
    @Test
    void 재고보다_이하인_수량_소비_테스트() {
        // given
        final long REQUESTED_QUANTITY = INITIAL_STOCK / 2;
        List<OrderItemKafkaEvent> requestedEvents = new ArrayList<>();
        itemIds.forEach(id -> {
            requestedEvents.add(getOrderItemKafkaEvent(id, REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING));
        });

        // when
        List<OrderItemKafkaEvent> responseEvents = requestedEvents.stream()
                .map(event -> stockUpdateByRedissonServiceImpl.updateStock(event, ItemProcessingStatus.STOCK_CONSUMPTION))
                .toList();

        // then
        List<Optional<Item>> items = itemIds.stream()
                .map(itemRepository::findById)
                .toList();

        assert items.stream().allMatch(Optional::isPresent);
        assertThat(items).allMatch(
                item -> {
                    assert item.isPresent();
                    return item.get().getStock().equals(INITIAL_STOCK - REQUESTED_QUANTITY);
                }
        );
        assertThat(responseEvents).allMatch(responseEvent -> responseEvent.getOrderProcessingStatus().equals(OrderProcessingStatus.SUCCESSFUL));
    }

    @DisplayName("아이템 재고보다 많은 수량으로 차감하면 DB에 반영되면 안 됨")
    @Test
    void 재고보다_초과된_수량_소비_테스트() {
        // given
        final long REQUESTED_QUANTITY = INITIAL_STOCK + 100L;
        final long targetItemId = itemIds.get(0);
        OrderItemKafkaEvent request = getOrderItemKafkaEvent(targetItemId, REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING);

        // when
        OrderItemKafkaEvent response = stockUpdateByRedissonServiceImpl.updateStock(request, ItemProcessingStatus.STOCK_CONSUMPTION);

        // then
        assertThat(response.getItemId()).isEqualTo(targetItemId);
        Item item = itemRepository.findById(targetItemId)
                .orElseThrow(() -> new EntityNotFoundException("{} 아이템 존재하지 않음" + targetItemId));

        assertThat(item.getStock()).isEqualTo(INITIAL_STOCK);
        assertThat(response.getOrderProcessingStatus()).isEqualTo(OrderProcessingStatus.OUT_OF_STOCK);
    }
}