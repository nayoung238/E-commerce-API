package com.ecommerce.accountservice.api.dto;

import com.ecommerce.accountservice.domain.Account;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

@Getter
public class AccountDto {

    private final Long accountId;
    private final String email;
    private final String name;

    @Builder(access = AccessLevel.PRIVATE)
    private AccountDto(Long accountId, String email, String name) {
        this.accountId = accountId;
        this.email = email;
        this.name = name;
    }

    public static AccountDto of(Account account) {
        return AccountDto.builder()
                .accountId(account.getId())
                .email(account.getEmail())
                .name(account.getName())
                .build();
    }
}