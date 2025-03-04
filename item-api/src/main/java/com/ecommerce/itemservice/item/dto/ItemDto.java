package com.ecommerce.itemservice.item.dto;

import com.ecommerce.itemservice.item.entity.Item;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ItemDto (

    Long id,

    @NotBlank(message = "아이템 이름은 필수입니다.")
    String name,

    @NotNull(message = "아이템 재고는 필수입니다.")
    @Min(value = 0, message = "아이템 재고는 0 이상이어야 합니다.")
    Long stock,

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    Long price
) {

    public static ItemDto of(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .price(item.getPrice())
                .build();
    }
}
