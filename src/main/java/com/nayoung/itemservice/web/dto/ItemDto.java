package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.Item;
import lombok.Getter;

import java.util.Optional;

@Getter
public class ItemDto {

    private boolean isExist;
    private Long shopId;
    private String shopName;

    private Long itemId;
    private String itemName;
    private Long price;
    private Integer discountPercentage;
    private Long stock;

    private ItemDto() {
        this.isExist = false;
    }

    private ItemDto(Item item) {
        this.isExist = true;
        this.shopId = item.getShop().getId();
        this.shopName = item.getShop().getName();
        this.itemId = item.getId();
        this.itemName = item.getName();
        this.price = item.getPrice();
        this.discountPercentage = item.getDiscountPercentage();
        this.stock = item.getStock();
    }

    private static ItemDto fromItemEntity(Item item) {
        return new ItemDto(item);
    }

    public static ItemDto getInstance(Optional<Item> item) {
        return item.map(ItemDto::fromItemEntity).orElseGet(ItemDto::new);
    }
}
