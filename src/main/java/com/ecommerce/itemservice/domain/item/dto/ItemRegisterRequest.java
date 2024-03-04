package com.ecommerce.itemservice.domain.item.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemRegisterRequest {

    @NotBlank
    private String name;

    @NotNull
    private Long stock;
}
