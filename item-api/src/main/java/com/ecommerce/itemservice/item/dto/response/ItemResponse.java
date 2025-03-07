package com.ecommerce.itemservice.item.dto.response;

import com.ecommerce.itemservice.item.entity.Item;
import lombok.Builder;

@Builder
public record ItemResponse(

    Long id,
    String name,
    Long stock,
    Long price
) {

    public static ItemResponse of(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .price(item.getPrice())
                .build();
    }
}
