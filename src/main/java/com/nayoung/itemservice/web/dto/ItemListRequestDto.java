package com.nayoung.itemservice.web.dto;

import lombok.Builder;
import lombok.Getter;

import javax.validation.constraints.NotBlank;

@Getter
@Builder
public class ItemListRequestDto {

    @NotBlank
    private String itemName;

    private String location;
    private String customerRating;
}