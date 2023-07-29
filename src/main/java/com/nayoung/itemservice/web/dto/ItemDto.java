package com.nayoung.itemservice.web.dto;

import com.nayoung.itemservice.domain.item.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;
import java.util.Optional;

@Getter @Builder
@AllArgsConstructor
public class ItemDto {

    private Boolean isExist;

    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private Long shopId;

    @NotBlank
    private Long price;
    private Long discountedPrice;
    private Integer discountPercentage;

    @NotBlank
    private Long stock;

    private ItemDto() {
        this.isExist = false;
    }

    public static ItemDto fromItem(Item item) {
        return ItemDto.builder()
                .isExist(Boolean.TRUE)
                .id(item.getId())
                .name(item.getName())
                .shopId(item.getShop().getId())
                .price(item.getPrice())
                .discountPercentage(item.getDiscountPercentage())
                .stock(item.getStock())
                .build();
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public void setPrice(Long price) {
        this.price = price;
    }

    public void setDiscountedPrice(Long discountedPrice) {
        this.discountPercentage = discountPercentage;
    }

    public static ItemDto getInstance(Optional<Item> item) {
        return item.map(ItemDto::fromItem).orElseGet(ItemDto::new);
    }
}
