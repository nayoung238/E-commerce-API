package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.Item;
import lombok.Getter;

@Getter
public class ItemResponse {

    private Long id;
    private String name;
    private Long price;
    private Long discountedPrice;
    private Long stock;


    public ItemResponse(Item item, int discountPercentage) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.discountedPrice = this.price * (100 - discountPercentage) / 100;
        this.stock = item.getStock();
    }

    public static ItemResponse fromItemEntity(Item item) {
        return new ItemResponse(item, 0);
    }

    public static ItemResponse fromItemEntityAndApplyDiscount(Item item, int discountPercentage) {
        return new ItemResponse(item, discountPercentage);
    }
}
