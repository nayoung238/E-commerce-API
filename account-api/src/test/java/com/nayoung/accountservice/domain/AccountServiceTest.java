package com.nayoung.accountservice.domain;

import com.ecommerce.accountservice.domain.AccountRepository;
import com.ecommerce.accountservice.domain.AccountService;
import com.ecommerce.accountservice.exception.AccountException;
import com.ecommerce.accountservice.api.dto.AccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
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
