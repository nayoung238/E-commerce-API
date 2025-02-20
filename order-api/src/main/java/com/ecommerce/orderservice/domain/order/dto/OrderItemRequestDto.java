package com.ecommerce.orderservice.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record OrderItemRequestDto (

    @NotNull(message = "아이템 아이디는 필수입니다.")
    @Min(value = 1, message = "아이템 아이디는 1 이상이어야 합니다.")
    @Schema(description = "아이템 아이디", nullable = false)
    Long itemId,

    @NotNull(message = "주문 수량은 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    @Schema(description = "주문 수량", nullable = false)
    Long quantity
) { }
