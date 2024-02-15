package com.ecommerce.accountservice.web.dto;

import com.ecommerce.accountservice.domain.Account;
import lombok.Getter;

@Getter
public class AccountDto {

    private Long accountId;
    private String email;
    private String name;

    private AccountDto(Account account) {
        this.accountId = account.getId();
        this.email = account.getEmail();
        this.name = account.getName();
    }

    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account);
    }
}