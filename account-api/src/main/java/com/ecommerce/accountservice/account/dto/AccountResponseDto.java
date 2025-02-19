package com.ecommerce.accountservice.account.dto;

import com.ecommerce.accountservice.account.entity.Account;
import lombok.Builder;

@Builder
public record AccountResponseDto (

    String loginId,
    String name
) {

    public static AccountResponseDto of(Account account) {
        return AccountResponseDto.builder()
                .loginId(account.getLoginId())
                .name(account.getName())
                .build();
    }
}