package com.ecommerce.orderservice.order.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record OrderCreationRequest(

    @NotNull(message = "사용자 아이디는 필수입니다.")
    @Min(value = 1, message = "사용자 아이디는 1 이상이어야 합니다.")
    @Schema(description = "사용자 아이디", nullable = false)
    Long userId,

    @NotNull(message = "주문 아이템은 필수입니다.")
    @Valid
    @Size(min = 1, message = "주문 아이템은 필수입니다")
    @Schema(description = "주문 아이템 목록", nullable = false)
    List<OrderItemRequest> orderItems
) {

    public static OrderCreationRequest of(Long userId, List<OrderItemRequest> orderItems) {
        return OrderCreationRequest.builder()
                .userId(userId)
                .orderItems(orderItems)
                .build();
    }
}
