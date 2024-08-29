package com.ecommerce.itemservice.domain.item.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemRegisterRequest {

    @NotBlank(message = "아이템 이름은 필수입니다.")
    private String name;

    @NotNull(message = "아이템 재고는 필수입니다.")
    @Min(value = 0, message = "아이템 재고는 0 이상이어야 합니다.")
    private Long stock;

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    private Long price;
}
