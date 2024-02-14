package com.ecommerce.accountservice.domain;

import com.ecommerce.accountservice.openfeign.OrderServiceClient;
import com.ecommerce.accountservice.web.dto.AccountDto;
import com.ecommerce.accountservice.openfeign.client.OrderDto;
import com.ecommerce.accountservice.web.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public AccountDto createAccount(SignUpDto signUpDto) {
        Account account = Account.fromAccountDto(signUpDto);
        account = accountRepository.save(account);
        return AccountDto.fromAccount(account);
    }

    public AccountDto getAccountById(Long id, @Nullable Long cursorOrderId) {
        Account account = accountRepository.findById(id).orElseThrow();

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<OrderDto> orderDtos = circuitBreaker.run(() -> orderServiceClient.getOrders(id, cursorOrderId),
                                                        throwable -> new ArrayList<>());

        AccountDto result = AccountDto.fromAccount(account);
        result.setOrderDtos(orderDtos);
        return result;
    }
}
