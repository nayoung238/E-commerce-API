package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.exception.AccountException;
import com.ecommerce.accountservice.exception.ExceptionCode;
import com.ecommerce.accountservice.api.dto.AccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    public AccountDto createAccount(SignUpRequestDto signUpRequestDto) {
        Account account = Account.of(signUpRequestDto);
        account = accountRepository.save(account);
        return AccountDto.of(account);
    }

    public AccountDto getAccountById(Long userId) {
        Account account = accountRepository.findById(userId)
                .orElseThrow(() -> new AccountException(ExceptionCode.NOT_FOUND_ACCOUNT));
        return AccountDto.of(account);
    }
}
