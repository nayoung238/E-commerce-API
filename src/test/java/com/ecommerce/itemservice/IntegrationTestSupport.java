package com.ecommerce.itemservice;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@EmbeddedKafka(
        partitions = 2,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092"
        },
        ports = {9092})
@ActiveProfiles("test")
@Slf4j
public class IntegrationTestSupport {

    @Autowired
    ItemRepository itemRepository;

    @Autowired
    ItemRedisRepository itemRedisRepository;

    protected Item getItem(String name, long stock, long price) {
        return Item.builder()
                .name(name)
                .stock(stock)
                .price(price)
                .build();
    }

    protected OrderKafkaEvent getOrderKafkaEvent(long accountId, List<Long> itemIds, long quantity, OrderStatus status) {
        List<OrderItemKafkaEvent> orderItemKafkaEvents = itemIds.stream()
                .map(i -> getOrderItemKafkaEvent(i, quantity, status))
                .toList();

        return OrderKafkaEvent.builder()
                .orderEventId(getOrderEventId(accountId))
                .orderStatus(status)
                .orderItemKafkaEvents(orderItemKafkaEvents)
                .accountId(accountId)
                .createdAt(LocalDateTime.now())
                .requestedAt(LocalDateTime.now())
                .build();
    }

    private String getOrderEventId(long accountId) {
        return accountId + "-" + UUID.randomUUID().toString().substring(0, 8);
    }


    /*
        OrderStatus 설정 이유: Order-Service에서 OrderStatus.WAITING 설정해서 이벤트 전송함
     */
    protected OrderItemKafkaEvent getOrderItemKafkaEvent(long itemId, long quantity, OrderStatus status) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderStatus(status)
                .build();
    }

    protected void saveItem(Item item) {
        itemRepository.save(item);
        itemRedisRepository.initializeItemStock(item.getId(), item.getStock());
    }
}
