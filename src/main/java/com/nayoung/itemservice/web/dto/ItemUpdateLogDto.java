package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.messagequeue.client.OrderItemStatus;
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
