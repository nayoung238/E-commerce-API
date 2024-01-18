package com.nayoung.itemservice.web.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemListRequestDto {

    @NotBlank
    private String itemName;

    private String location;
    private String customerRating;
}