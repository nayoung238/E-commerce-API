package com.ecommerce.accountservice.api.dto;

import com.ecommerce.accountservice.domain.Account;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class SimpleAccountDto {

    private final long accountId;
    private final String email;
    private final String name;
    private final long numberOfCoupons;

    @Builder(access = AccessLevel.PRIVATE)
    private SimpleAccountDto(Long accountId, String email, String name, long numberOfCoupons) {
        this.accountId = accountId;
        this.email = email;
        this.name = name;
        this.numberOfCoupons = numberOfCoupons;
    }

    public static SimpleAccountDto of(Account account) {
        return SimpleAccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .name(account.getName())
                .numberOfCoupons(account.getCoupons().size())
                .build();
    }
}