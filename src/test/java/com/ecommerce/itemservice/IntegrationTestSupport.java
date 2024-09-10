package com.ecommerce.itemservice;

import com.ecommerce.itemservice.domain.item.Item;
import com.ecommerce.itemservice.kafka.dto.OrderItemKafkaEvent;
import com.ecommerce.itemservice.kafka.dto.OrderStatus;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;

@EmbeddedKafka(
        partitions = 2,
        brokerProperties = {
                "listeners=PLAINTEXT://localhost:9092"
        },
        ports = {9092})
@ActiveProfiles("test")
public class IntegrationTestSupport {

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
    protected OrderItemKafkaEvent getOrderItemKafkaEvent(long itemId, long quantity, OrderStatus status) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderStatus(status)
                .build();
    }
}
