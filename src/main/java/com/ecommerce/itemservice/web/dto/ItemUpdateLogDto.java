package com.ecommerce.itemservice.web.dto;

import com.ecommerce.itemservice.kafka.dto.OrderItemStatus;
import com.ecommerce.itemservice.domain.item.ItemUpdateLog;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ItemUpdateLogDto {

    private Long id;
    private String eventId;
    private OrderItemStatus orderItemStatus;
    private Long itemId;
    private Long quantity;

    public static ItemUpdateLogDto fromItemUpdateLog(ItemUpdateLog itemUpdateLog) {
        return ItemUpdateLogDto.builder()
                .id(itemUpdateLog.getId())
                .eventId(itemUpdateLog.getEventId())
                .orderItemStatus(itemUpdateLog.getOrderItemStatus())
                .itemId(itemUpdateLog.getItemId())
                .quantity(itemUpdateLog.getQuantity())
                .build();
    }
}
