package com.nayoung.itemservice.web.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nayoung.itemservice.domain.item.log.OrderStatus;
import lombok.Builder;
import lombok.Getter;
import java.util.Map;

@Getter @Builder
public class ItemStockToUpdateDto {

    private Long orderId;
    private OrderStatus orderStatus;
    private Long customerAccountId;
    private Long shopId;
    private Long itemId;
    private Long quantity;

    public static ItemStockToUpdateDto fromKafkaMessage(Object message) {
        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Long> map = objectMapper.convertValue(message, Map.class);

        return ItemStockToUpdateDto.builder()
                .shopId(Long.parseLong(String.valueOf(map.get("shopId"))))
                .itemId(Long.parseLong(String.valueOf(map.get("itemId"))))
                .quantity(Long.parseLong(String.valueOf(map.get("quantity"))))
                .build();
    }

    public static ItemStockToUpdateDto fromOrderItemRequest(OrderStatus orderStatus, ItemStockToUpdateDto request) {
        return ItemStockToUpdateDto.builder()
                .orderStatus(orderStatus)
                .shopId(request.getShopId())
                .itemId(request.getItemId())
                .quantity(request.getQuantity())
                .build();
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }
}
