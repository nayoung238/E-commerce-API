package com.ecommerce.accountservice.domain.service;

import com.ecommerce.accountservice.domain.Account;
import com.ecommerce.accountservice.domain.repo.AccountRepository;
import com.ecommerce.accountservice.exception.ExceptionCode;
import com.ecommerce.accountservice.kafka.dto.CouponIssuanceResultKafkaEvent;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CouponService {

    private final AccountRepository accountRepository;

    @Transactional
    public void addCoupon(CouponIssuanceResultKafkaEvent event) {
        Account account = accountRepository.findById(event.getAccountId())
                .orElseThrow(() -> new EntityNotFoundException(ExceptionCode.NOT_FOUND_ACCOUNT.getMessage()));

        account.addCoupon(event.getCouponId(), event.getCouponName());
    }
}
