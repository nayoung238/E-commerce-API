package com.nayoung.itemservice.messagequeue.client;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemDto {

    private Long id;
    private Long itemId;
    private Long quantity;
    private Long price;
    private Long shopId;
    private OrderItemStatus orderItemStatus;

    public static OrderItemDto from(ItemUpdateLog itemUpdateLog) {
        return OrderItemDto.builder()
                .itemId(itemUpdateLog.getItemId())
                .quantity(itemUpdateLog.getQuantity())
                .orderItemStatus(itemUpdateLog.getOrderItemStatus())
                .build();
    }

    public void setOrderItemStatus(OrderItemStatus orderItemStatus) {
        this.orderItemStatus = orderItemStatus;
    }

    public void convertSign() {
        quantity *= -1;
    }
}
