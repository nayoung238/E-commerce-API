package com.ecommerce.apigatewayservice.service.mypage.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
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

    @JsonProperty("numberOfCoupons")
    private Long numberOfCoupons;

    @JsonProperty("orders")
    private List<OrderSimpleDto> orders;

    @Builder(access = AccessLevel.PRIVATE)
    private MyPageDto(long accountId, String email, String name, long numberOfCoupons, OrderListDto orders) {
        this.accountId = accountId;
        this.email = email;
        this.name = name;
        this.numberOfCoupons = numberOfCoupons;
        this.orders = orders.getOrderSimpleDtoList();
    }

    public static MyPageDto of(SimpleAccountDto account, OrderListDto orders) {
        return MyPageDto.builder()
                .accountId(account.getAccountId())
                .email(account.getEmail())
                .name(account.getName())
                .numberOfCoupons(account.getNumberOfCoupons())
                .orders(orders)
                .build();
    }
}
