package com.nayoung.accountservice.web.dto;

import com.nayoung.accountservice.domain.Account;
import lombok.Data;

@Data
public class AccountResponse {

    private Long accountId;
    private String email;
    private String name;

    private AccountResponse(Account account) {
        this.accountId = account.getId();
        this.email = account.getEmail();
        this.name = account.getName();
    }

    public static AccountResponse fromAccountEntity(Account account) {
        return new AccountResponse(account);
    }
}
