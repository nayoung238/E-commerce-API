package com.ecommerce.accountservice.api.dto;

import com.ecommerce.accountservice.domain.Account;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public record AccountResponseDto(

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