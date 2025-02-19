package com.ecommerce.accountservice.account.service;

import com.ecommerce.accountservice.account.dto.AccountResponseDto;
import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import com.ecommerce.accountservice.account.entity.Account;
import com.ecommerce.accountservice.account.repository.AccountRepository;
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
    public AccountResponseDto createAccount(SignUpRequestDto signUpRequestDto) {
        Account account = Account.of(signUpRequestDto);
        accountRepository.save(account);
        return AccountResponseDto.of(account);
    }

    public AccountResponseDto findAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ACCOUNT.getMessage()));

        return AccountResponseDto.of(account);
    }
}
