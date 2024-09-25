package com.ecommerce.accountservice.domain.service;

import com.ecommerce.accountservice.api.dto.DetailedAccountDto;
import com.ecommerce.accountservice.api.dto.SimpleAccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import com.ecommerce.accountservice.domain.Account;
import com.ecommerce.accountservice.domain.repo.AccountRepository;
import com.ecommerce.accountservice.exception.ExceptionCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public SimpleAccountDto createAccount(SignUpRequestDto signUpRequestDto) {
        Account account = Account.of(signUpRequestDto);
        accountRepository.save(account);
        return SimpleAccountDto.of(account);
    }

    public SimpleAccountDto findSimpleAccountInfoById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ACCOUNT.getMessage()));

        return SimpleAccountDto.of(account);
    }

    public DetailedAccountDto findDetailedAccountInfoById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ACCOUNT.getMessage()));

        return DetailedAccountDto.of(account);
    }
}
