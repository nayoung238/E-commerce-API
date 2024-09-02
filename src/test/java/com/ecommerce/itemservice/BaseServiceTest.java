package com.ecommerce.itemservice;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.domain.item.repository.ItemRedisRepository;
import com.ecommerce.itemservice.domain.item.repository.ItemRepository;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Slf4j
public class BaseServiceTest {

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

    /*
        OrderStatus 설정 이유: Order-Service에서 OrderStatus.WAITING 설정해서 이벤트 전송함
     */
    protected OrderItemKafkaEvent getOrderItemEvent(long itemId, long quantity, OrderStatus orderStatus) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderStatus(orderStatus)
                .build();
    }

    protected void saveItem(Item item) {
        itemRepository.save(item);
        itemRedisRepository.initializeItemStock(item.getId(), item.getStock());
    }
}
