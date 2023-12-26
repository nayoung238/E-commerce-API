package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter @Builder
@AllArgsConstructor
public class ItemDto {

    private Boolean isExist;

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private Long stock;

    public static ItemDto fromItem(Item item) {
        return ItemDto.builder()
                .isExist(Boolean.TRUE)
                .id(item.getId())
                .name(item.getName())
                .stock(item.getStock())
                .build();
    }
}
