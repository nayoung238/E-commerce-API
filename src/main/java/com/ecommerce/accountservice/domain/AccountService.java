package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.api.dto.DetailedAccountDto;
import com.ecommerce.accountservice.api.dto.SimpleAccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
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
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        return SimpleAccountDto.of(account);
    }

    public DetailedAccountDto findDetailedAccountInfoById(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new EntityNotFoundException("Account not found"));

        return DetailedAccountDto.of(account);
    }
}
