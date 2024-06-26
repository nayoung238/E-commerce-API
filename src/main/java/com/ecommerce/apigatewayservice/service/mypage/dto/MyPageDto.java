package com.ecommerce.apigatewayservice.service.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.util.List;

@Getter
public class MyPageDto {

    @JsonProperty("accountId")
    private Long accountId;

    @JsonProperty("email")
    private String email;

    @JsonProperty("name")
    private String name;

    @JsonProperty("orders")
    private List<OrderSimpleDto> orders;

    public MyPageDto(AccountDto account, OrderListDto orders) {
        this.accountId = account.getAccountId();
        this.email = account.getEmail();
        this.name = account.getName();
        this.orders = orders.getOrderSimpleDtoList();
    }
}
