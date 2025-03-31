package com.ecommerce.itemservice.kafka.dto;

import com.ecommerce.itemservice.item.enums.ItemProcessingStatus;
import lombok.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class OrderItemKafkaEvent {

    private Long itemId;
    private Long quantity;
    private OrderStatus orderStatus;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemKafkaEvent(long itemId, long quantity, OrderStatus orderStatus) {
        this.itemId = itemId;
        this.quantity = quantity;
        this.orderStatus = orderStatus;
    }

    // Test 코드에서 사용
    public static OrderItemKafkaEvent of(long itemId, long quantity, OrderStatus orderStatus) {
        return OrderItemKafkaEvent.builder()
                .itemId(itemId)
                .quantity(quantity)
                .orderStatus(orderStatus)
                .build();
    }

    public void updateOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public void updateOrderStatus(ItemProcessingStatus itemProcessingStatus) {
        this.orderStatus = OrderStatus.getStatus(itemProcessingStatus);
    }
}
