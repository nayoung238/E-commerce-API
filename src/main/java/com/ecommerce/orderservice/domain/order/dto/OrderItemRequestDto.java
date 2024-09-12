package com.ecommerce.orderservice.domain.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItemRequestDto {

    @NotNull(message = "아이템 아이디는 필수입니다.")
    @Min(value = 1, message = "아이템 아이디는 1 이상이어야 합니다.")
    @Schema(description = "아이템 아이디", nullable = false)
    private Long itemId;

    @NotNull(message = "주문 수량은 필수입니다.")
    @Min(value = 1, message = "주문 수량은 1 이상이어야 합니다.")
    @Schema(description = "주문 수량", nullable = false)
    private Long quantity;

    @Builder(access = AccessLevel.PRIVATE)
    private OrderItemRequestDto(long itemId, long quantity) {
        this.itemId = itemId;
        this.quantity = quantity;
    }

    // Test 코드에서 사용
    public static OrderItemRequestDto of(long itemId, long quantity) {
        return OrderItemRequestDto.builder()
                .itemId(itemId)
                .quantity(quantity)
                .build();
    }
}
