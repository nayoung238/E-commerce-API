package com.ecommerce.accountservice.api.dto;

import com.ecommerce.accountservice.domain.Account;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class DetailedAccountDto {

    private final long accountId;
    private final String email;
    private final String name;
    private final List<SimpleCouponDto> coupons;

    @Builder(access = AccessLevel.PRIVATE)
    private DetailedAccountDto(long accountId, String email, String name, List<SimpleCouponDto> coupons) {
        this.accountId = accountId;
        this.email = email;
        this.name = name;
        this.coupons = coupons;
    }

    public static DetailedAccountDto of(Account account) {
        List<SimpleCouponDto> coupons = account.getCoupons()
                .entrySet()
                .stream()
                .map(c -> SimpleCouponDto.of(c.getKey(), c.getValue()))
                .toList();

        return DetailedAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .name(account.getName())
                .coupons(coupons)
                .build();
    }
}
