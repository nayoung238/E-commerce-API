package com.nayoung.accountservice.domain;

import com.nayoung.accountservice.client.OrderServiceClient;
import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.OrderResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final OrderServiceClient orderServiceClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    public AccountResponse createAccount(SignUpRequest signUpRequest) {
        Account account = Account.fromAccountDto(signUpRequest);
        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromAccountEntity(savedAccount);
    }

    public AccountResponse getAccountById(Long id, Long cursorOrderId) {
        Account account = accountRepository.findById(id).orElseThrow();
        AccountResponse response = AccountResponse.fromAccountEntity(account);

        CircuitBreaker circuitBreaker = circuitBreakerFactory.create("circuitbreaker");
        List<OrderResponse> orders = circuitBreaker.run(() -> orderServiceClient.getOrders(id, cursorOrderId),
                                                        throwable -> new ArrayList<>());

        response.setOrders(orders);
        return response;
    }
}
