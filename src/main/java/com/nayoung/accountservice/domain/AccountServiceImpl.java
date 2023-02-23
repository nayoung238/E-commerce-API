package com.nayoung.accountservice.domain;

import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.OrderResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RestTemplate restTemplate;
    private final Environment environment;

    @Override
    public AccountResponse createAccount(SignUpRequest signUpRequest) {
        Account account = Account.fromAccountDto(signUpRequest);
        Account savedAccount = accountRepository.save(account);
        return AccountResponse.fromAccountEntity(savedAccount);
    }

    @Override
    public AccountResponse getAccountById(Long id) {
        Account account = accountRepository.findById(id).orElseThrow();
        AccountResponse response = AccountResponse.fromAccountEntity(account);

        String orderUrl = String.format(environment.getProperty("order_service.url"), id);
        ResponseEntity<List<OrderResponse>> orderResponse =
                restTemplate.exchange(orderUrl, HttpMethod.GET, null,
                        new ParameterizedTypeReference<List<OrderResponse>>() {
                        });

        response.setOrders(orderResponse.getBody());
        return response;
    }
}
