package com.ecommerce.itemservice.domain.item.dto;

import com.ecommerce.itemservice.domain.item.Item;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Builder
@AllArgsConstructor
@NoArgsConstructor
public class ItemDto {

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private Long stock;

    public static ItemDto fromItem(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .build();
    }
}
