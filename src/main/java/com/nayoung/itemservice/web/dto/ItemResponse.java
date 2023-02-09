package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.Item;
import lombok.Data;

@Data
public class ItemResponse {

    private Long id;
    private String name;
    private Long price;
    private Long stock;

    private ItemResponse(Item item) {
        this.id = item.getId();
        this.name = item.getName();
        this.price = item.getPrice();
        this.stock = item.getStock();
    }

    public static ItemResponse fromItemEntity(Item item) {
        return new ItemResponse(item);
    }
}
