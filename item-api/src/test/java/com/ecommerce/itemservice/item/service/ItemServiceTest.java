package com.ecommerce.itemservice.item.service;

import com.ecommerce.itemservice.item.entity.Item;
import com.ecommerce.itemservice.item.repository.ItemRepository;
import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderProcessingStatus;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@EmbeddedKafka(
    partitions = 2,
    brokerProperties = {
        "listeners=PLAINTEXT://localhost:9092"
    },
    ports = {9092}
)
@SpringBootTest
@ActiveProfiles("test")
class ItemServiceTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private ItemRepository itemRepository;

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
    }

    @DisplayName("[재고 차감 성공 테스트] 아이템 재고보다 적은 수량으로 차감하면 DB에 값이 반영되어야 함")
    @Test
    void decrease_stock_success_test() {
        // given
        final long INITIAL_STOCK = 100L;
        Item item = Item.of("test-item-1", INITIAL_STOCK, 1000);
        itemRepository.save(item);

        final long REQUESTED_QUANTITY = INITIAL_STOCK / 2;
        OrderItemKafkaEvent event = OrderItemKafkaEvent.of(item.getId(), REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING);

        // when
        OrderItemKafkaEvent response = itemService.updateStockByOptimisticLock(event, ItemProcessingStatus.STOCK_CONSUMPTION);

        // then
        assertThat(response.getOrderProcessingStatus()).isEqualTo(OrderProcessingStatus.SUCCESSFUL);

        Optional<Item> itemOptional = itemRepository.findById(item.getId());
        assertThat(itemOptional.isPresent()).isTrue();
        assertThat(itemOptional.get().getStock()).isEqualTo(INITIAL_STOCK - REQUESTED_QUANTITY);
    }

    @DisplayName("[재고 차감 실패 테스트] 아이템 재고보다 많은 수량으로 차감하면 DB에 반영되지 않음")
    @Test
    void decrease_stock_failed_test() {
        // given
        final long INITIAL_STOCK = 10L;
        Item item = Item.of("test-item-2", INITIAL_STOCK, 1000);
        itemRepository.save(item);

        final long REQUESTED_QUANTITY = INITIAL_STOCK + 100L;
        OrderItemKafkaEvent request = OrderItemKafkaEvent.of(item.getId(), REQUESTED_QUANTITY, OrderProcessingStatus.PROCESSING);

        // when
        OrderItemKafkaEvent response = itemService.updateStockByOptimisticLock(request, ItemProcessingStatus.STOCK_CONSUMPTION);

        // then
        assertThat(response.getOrderProcessingStatus()).isEqualTo(OrderProcessingStatus.OUT_OF_STOCK);

        Optional<Item> itemOptional = itemRepository.findById(item.getId());
        assertThat(itemOptional.isPresent()).isTrue();
        assertThat(itemOptional.get().getStock()).isEqualTo(INITIAL_STOCK);
    }
}