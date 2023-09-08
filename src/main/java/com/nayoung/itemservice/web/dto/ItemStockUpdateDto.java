package com.nayoung.itemservice.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.log.ItemUpdateLog;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter @Builder
public class ItemStockUpdateDto {

    private Long orderId;
    private OrderStatus orderStatus;
    private Long customerAccountId;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    public static ItemStockUpdateDto fromKafkaMessage(Long orderId, Long customerAccountId, Object orderItem) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.convertValue(orderItem, Map.class);

        return ItemStockUpdateDto.builder()
                .orderId(orderId)
                .customerAccountId(customerAccountId)
                .shopId(Long.parseLong(String.valueOf(map.get("shopId"))))
                .itemId(Long.parseLong(String.valueOf(map.get("itemId"))))
                .quantity(Long.parseLong(String.valueOf(map.get("quantity"))))
                .build();
    }

    public static ItemStockUpdateDto fromOrderItemRequest(OrderStatus orderStatus, ItemStockUpdateDto request) {
        return ItemStockUpdateDto.builder()
                .orderId(request.getOrderId())
                .orderStatus(orderStatus)
                .shopId(request.getShopId())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .build();
    }

    public static ItemStockUpdateDto fromItemUpdateLog(OrderStatus orderStatus, ItemUpdateLog itemUpdateLog) {
        return ItemStockUpdateDto.builder()
                .orderId(itemUpdateLog.getOrderId())
                .orderStatus(orderStatus)
                .shopId(itemUpdateLog.getShopId())
                .itemId(itemUpdateLog.getItemId())
                .quantity(-itemUpdateLog.getQuantity())
                .build();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
