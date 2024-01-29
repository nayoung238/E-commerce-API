package com.nayoung.itemservice.kafka.dto;

import com.nayoung.itemservice.domain.item.ItemUpdateLog;
import lombok.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private Long quantity;
    @Setter
    private OrderItemStatus orderItemStatus;

    public static OrderItemDto from(ItemUpdateLog itemUpdateLog) {
        return OrderItemDto.builder()
                .itemId(itemUpdateLog.getItemId())
                .quantity(itemUpdateLog.getQuantity())
                .orderItemStatus(itemUpdateLog.getOrderItemStatus())
                .build();
    }

    public void convertSign() {
        quantity *= -1;
    }
}
