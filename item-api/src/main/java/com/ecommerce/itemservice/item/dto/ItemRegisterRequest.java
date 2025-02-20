package com.ecommerce.itemservice.item.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record ItemRegisterRequest (

    @NotBlank(message = "아이템명은 필수입니다.")
    @Schema(description = "아이템명", nullable = false)
    String name,

    @NotNull(message = "아이템 재고는 필수입니다.")
    @Min(value = 0, message = "아이템 재고는 0 이상이어야 합니다.")
    @Schema(description = "아이템 재고", nullable = false)
    Long stock,

    @NotNull(message = "가격은 필수입니다.")
    @Min(value = 0, message = "가격은 0 이상이어야 합니다.")
    @Schema(description = "아이템 가격", nullable = false)
    Long price
) { }
