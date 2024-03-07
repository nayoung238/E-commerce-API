package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
@ActiveProfiles("local")
class StockUpdateByRedissonServiceImplTest {

    @Autowired
    private StockUpdateByRedissonServiceImpl service;

    @Autowired
    private ItemRepository itemRepository;

    private static final int NUMBER_OF_ITEMS = 2;
    private static final Long INITIAL_STOCK = 100L;

    @BeforeEach
    void beforeEach() {
        IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .forEach(i -> {
                    Item item = Item.builder().name("item-" + i).stock(INITIAL_STOCK).build();
                    item = itemRepository.save(item);
                });
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
    }

    @Test
    void 재고_변경_테스트() {
        // given
        final Long REQUESTED_QUANTITY = 10L;
        List<OrderItemEvent> orderItemEvents = getOrderItemEvents(REQUESTED_QUANTITY);

        // when
        List<OrderItemEvent> result = orderItemEvents.stream()
                .map(o -> service.updateStock(o))
                .toList();

        // then
        assert result.size() == orderItemEvents.size();
        assertTrue(result.stream()
                .allMatch(o -> o.getOrderStatus().equals(OrderStatus.SUCCEEDED)));

        result.forEach(o -> {
            Item item = itemRepository.findById(o.getItemId())
                    .orElseThrow(() -> new IllegalArgumentException("id="+ o.getItemId() + " 상품 존재하지 않음"));
            assertEquals(INITIAL_STOCK - REQUESTED_QUANTITY, item.getStock());
        });
    }

    private List<OrderItemEvent> getOrderItemEvents(Long requestedQuantity) {
        return IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .mapToObj(i -> OrderItemEvent.builder()
                        .itemId((long) i)
                        .quantity(-requestedQuantity)
                        .build()
                ).toList();
    }
}