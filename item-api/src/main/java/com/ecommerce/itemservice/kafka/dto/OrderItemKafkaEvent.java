package com.ecommerce.itemservice.kafka.dto;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderItemKafkaEvent {

    private Long itemId;
    private Long quantity;
    private OrderProcessingStatus orderProcessingStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemKafkaEvent(long itemId, long quantity, OrderProcessingStatus orderProcessingStatus) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderProcessingStatus = orderProcessingStatus;
    }

    // Test 코드에서 사용
    public static OrderItemKafkaEvent of(long itemId, long quantity, OrderProcessingStatus orderProcessingStatus) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderProcessingStatus(orderProcessingStatus)
                .build();
    }

    public void updateOrderProcessingStatus(OrderProcessingStatus orderProcessingStatus) {
        this.orderProcessingStatus = orderProcessingStatus;
    }

    public void updateOrderProcessingStatus(ItemProcessingStatus itemProcessingStatus) {
        this.orderProcessingStatus = OrderProcessingStatus.getStatus(itemProcessingStatus);
    }
}
