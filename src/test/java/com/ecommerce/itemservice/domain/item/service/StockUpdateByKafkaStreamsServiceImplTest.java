package com.ecommerce.itemservice.domain.item.service;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.config.TopicConfig;
import com.ecommerce.itemservice.kafka.dto.OrderItemEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import com.ecommerce.itemservice.kafka.service.producer.KafkaProducerService;
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
class StockUpdateByKafkaStreamsServiceImplTest {

    @Autowired
    private StockUpdateByKafkaStreamsServiceImpl service;

    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ItemRedisRepository itemRedisRepository;

    @Autowired
    private KafkaProducerService kafkaProducerService;

    private static final int NUMBER_OF_ITEMS = 2;
    private static final Long INITIAL_STOCK = 100L;

    @BeforeEach
    void beforeEach() {
        IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .forEach(i -> {
                    Item item = Item.builder().name("item-" + i).stock(INITIAL_STOCK).build();
                    item = itemRepository.save(item);
                    itemRedisRepository.initializeItemStock(item.getId(), INITIAL_STOCK);
                });
    }

    @AfterEach
    void afterEach() {
        itemRepository.deleteAll();
        IntStream.rangeClosed(1, NUMBER_OF_ITEMS)
                .forEach(i -> itemRedisRepository.deleteKey((long) i));
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

        // Redis 데이터 확인
        result.forEach(o -> {
            Long stock = itemRedisRepository.findItemStock(o.getItemId());
            assertEquals(INITIAL_STOCK - REQUESTED_QUANTITY, stock);
        });

        try {
            sendDummyData();
            Thread.sleep(20000); // window-size=5s, grace-period=5s
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }

        // DB 데이터 확인
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

    private void sendDummyData() {
        IntStream.rangeClosed(1, NUMBER_OF_ITEMS).forEach(i -> {
            IntStream.rangeClosed(1, 100).forEach(j ->
                    kafkaProducerService.sendMessage(TopicConfig.ITEM_UPDATE_LOG_TOPIC, String.valueOf(i), 0L)
            );
        });
    }
}