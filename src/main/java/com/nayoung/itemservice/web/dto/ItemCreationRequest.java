package com.nayoung.itemservice.web.dto;

import lombok.Data;

@Data
public class ItemCreationRequest {

    private String name;
    private Long price;
    private Long stock;
}
