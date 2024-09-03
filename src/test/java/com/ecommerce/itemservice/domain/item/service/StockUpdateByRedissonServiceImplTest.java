package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.IntegrationTestSupport;
import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.*;

class StockUpdateByRedissonServiceImplTest extends IntegrationTestSupport {

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
    void 재고보다_이하인_수량_변경_테스트() {
        // given
        final long REQUESTED_QUANTITY = -(INITIAL_STOCK / 2);   // 차감이면 음수, 생산이면 양수 (e.g., -3: 수량 3개를 차감)
        List<OrderItemKafkaEvent> requestedEvents = new ArrayList<>();
        itemIds.forEach(id -> {
            requestedEvents.add(getOrderItemEvent(id, REQUESTED_QUANTITY, OrderStatus.WAITING));
        });

        // when
        List<OrderItemKafkaEvent> responseEvents = requestedEvents.stream()
                .map(stockUpdateByRedissonServiceImpl::updateStock)
                .toList();

        // then
        List<Optional<Item>> items = itemIds.stream()
                .map(itemRepository::findById)
                .toList();

        assert items.stream().allMatch(Optional::isPresent);
        assertThat(items).allMatch(
                item -> {
                    assert item.isPresent();
                    return item.get().getStock().equals(INITIAL_STOCK + REQUESTED_QUANTITY);  // 차감이면 REQUESTED_QUANTITY가 음수
                }
        );
        assertThat(responseEvents).allMatch(responseEvent -> responseEvent.getOrderStatus().equals(OrderStatus.SUCCEEDED));
    }

    @DisplayName("아이템 재고보다 많은 수량으로 차감하면 DB에 반영되면 안 됨")
    @Test
    void 재고보다_초과된_수량_변경_테스트() {
        // given
        final long REQUESTED_QUANTITY = -(INITIAL_STOCK + 100L);    // 차감이면 음수, 생산이면 양수 (e.g., -3: 수량 3개를 차감)
        final long targetItemId = itemIds.get(0);
        OrderItemKafkaEvent request = getOrderItemEvent(targetItemId, REQUESTED_QUANTITY, OrderStatus.WAITING);

        // when
        OrderItemKafkaEvent response = stockUpdateByRedissonServiceImpl.updateStock(request);

        // then
        assertThat(response.getItemId()).isEqualTo(targetItemId);
        Item item = itemRepository.findById(targetItemId)
                .orElseThrow(() -> new EntityNotFoundException("{} 아이템 존재하지 않음" + targetItemId));

        assertThat(item.getStock()).isEqualTo(INITIAL_STOCK);
        assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.OUT_OF_STOCK);
    }
}