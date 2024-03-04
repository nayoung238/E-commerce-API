package com.ecommerce.itemservice.domain.item.dto;

import com.ecommerce.itemservice.domain.item.Item;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank
    private String name;

    @NotNull
    private Long stock;

    public static ItemDto of(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .build();
    }
}
