package com.ecommerce.accountservice.account.service;

import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import com.ecommerce.accountservice.account.repository.AccountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class AccountServiceTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    void createAccount() {
        SignUpRequestDto request = SignUpRequestDto.of("abc@gmail.com", "nayoung", "password123");

        AccountDto response = accountService.createAccount(request);
        Assertions.assertEquals(request.getName(), response.getName());

        Assertions.assertThrows(AccountException.class,
                () -> accountService.getAccountById(1L));
        Assertions.assertThrows(AccountException.class,
                () -> accountService.getAccountById(2L));
    }
}
