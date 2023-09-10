package com.nayoung.itemservice.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.ItemUpdateStatus;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter @Builder
public class ItemStockUpdateDto {

    private ItemUpdateStatus itemUpdateStatus;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    public static ItemStockUpdateDto fromKafkaMessage(Long orderId, Long customerAccountId, Object orderItem) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.convertValue(orderItem, Map.class);

        return ItemStockUpdateDto.builder()
                .shopId(Long.parseLong(String.valueOf(map.get("shopId"))))
                .itemId(Long.parseLong(String.valueOf(map.get("itemId"))))
                .quantity(Long.parseLong(String.valueOf(map.get("quantity"))))
                .build();
    }

    public static ItemStockUpdateDto fromItemUpdateLog(ItemUpdateLog itemUpdateLog) {
        return ItemStockUpdateDto.builder()
                .itemUpdateStatus(itemUpdateLog.getItemUpdateStatus())
                .shopId(itemUpdateLog.getShopId())
                .itemId(itemUpdateLog.getItemId())
                .quantity(-itemUpdateLog.getQuantity())
                .build();
    }

    public void setItemUpdateStatus(ItemUpdateStatus itemUpdateStatus) {
        this.itemUpdateStatus = itemUpdateStatus;
    }
}
