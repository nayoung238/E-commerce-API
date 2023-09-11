package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import lombok.Builder;
import lombok.Getter;

@Getter @Builder
public class ItemUpdateLogDto {

    private Long id;
    private ItemUpdateStatus itemUpdateStatus;
    private Long orderId;
    private Long customerAccountId;
    private String createdAt;
    private Long itemId;
    private Long quantity;

    public static ItemUpdateLogDto fromItemUpdateLog(ItemUpdateLog itemUpdateLog) {
        return ItemUpdateLogDto.builder()
                .id(itemUpdateLog.getId())
                .itemUpdateStatus(itemUpdateLog.getItemUpdateStatus())
                .orderId(itemUpdateLog.getOrderId())
                .customerAccountId(itemUpdateLog.getCustomerAccountId())
                .itemId(itemUpdateLog.getItemId())
                .quantity(itemUpdateLog.getQuantity())
                .build();
    }
}
