package com.ecommerce.itemservice.domain.item.dto;

import com.ecommerce.itemservice.domain.item.Item;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
public class ItemDto {

    private Long id;

    @NotBlank(message = "아이템 이름은 필수입니다.")
    private String name;

    @NotNull(message = "아이템 재고는 필수입니다.")
    @Min(value = 0, message = "아이템 재고는 0 이상이어야 합니다.")
    private Long stock;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Long price;

    @Builder(access = AccessLevel.PRIVATE)
    private ItemDto(Long id, String name, Long stock, Long price) {
        this.id = id;
        this.name = name;
        this.stock = stock;
        this.price = price;
    }

    public static ItemDto of(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .price(item.getPrice())
                .build();
    }
}
