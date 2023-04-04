package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.Item;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ItemResponse {

    private boolean isExist;
    private Long shopId;
    private String shopName;

    private Long itemId;
    private String itemName;
    private Long price;
    private Long discountedPrice;
    private Long stock;

    @Builder
    public ItemResponse(boolean isExist, Long shopId, String shopName, Long itemId, String itemName, Long price, Long stock) {
        this.isExist = isExist;
        this.shopId = shopId;
        this.shopName = shopName;

        this.itemId = itemId;
        this.itemName = itemName;
        this.price = price;
        this.stock = stock;
    }

    public static ItemResponse fromItemEntity(Item item) {
        return ItemResponse.builder()
                .isExist(true)
                .shopId(item.getShop().getId())
                .shopName(item.getShop().getName())
                .itemId(item.getId())
                .itemName(item.getName())
                .price(item.getPrice())
                .stock(item.getStock())
                .build();
    }

    public static ItemResponse fromItemDto(ItemDto itemDto) {
        if(!itemDto.isExist()) return fromNotExist(itemDto);
        return ItemResponse.builder()
                .isExist(true)
                .shopId(itemDto.getShopId())
                .shopName(itemDto.getShopName())
                .itemId(itemDto.getItemId())
                .itemName(itemDto.getItemName())
                .price(itemDto.getPrice())
                .stock(itemDto.getStock())
                .build();
    }

    private static ItemResponse fromNotExist(ItemDto itemDto) {
        return ItemResponse.builder()
                .isExist(false)
                .shopId(itemDto.getShopId())
                .shopName(itemDto.getShopName())
                .build();
    }

    public void setDiscountedPrice(long discountedPrice) {
        this.discountedPrice = discountedPrice;
    }
}
