package com.ecommerce.accountservice.account.service;

import com.ecommerce.accountservice.account.dto.AccountResponseDto;
import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import com.ecommerce.accountservice.account.entity.Account;
import com.ecommerce.accountservice.account.repository.AccountRepository;
import com.ecommerce.accountservice.common.exception.CustomException;
import com.ecommerce.accountservice.common.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResponseDto createAccount(SignUpRequestDto request) {
        if(accountRepository.existsByLoginId(request.loginId())) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }

        try {
            Account account = Account.of(request);
            accountRepository.save(account);
            return AccountResponseDto.of(account);
        } catch (DataIntegrityViolationException e) {
            throw new CustomException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
    }

    public AccountResponseDto findAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ACCOUNT));

        return AccountResponseDto.of(account);
    }
}
