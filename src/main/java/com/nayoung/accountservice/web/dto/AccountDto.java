package com.nayoung.accountservice.web.dto;

import com.nayoung.accountservice.domain.Account;
import com.nayoung.accountservice.openfeign.client.OrderDto;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class AccountDto {

    private Long accountId;
    private String email;
    private String name;

    private List<OrderDto> orderDtos = new ArrayList<>();

    private AccountDto(Account account) {
        this.accountId = account.getId();
        this.email = account.getEmail();
        this.name = account.getName();
    }

    public static AccountDto fromAccount(Account account) {
        return new AccountDto(account);
    }

    public void setOrderDtos(List<OrderDto> orderDtos) {
        this.orderDtos = orderDtos;
    }
}
