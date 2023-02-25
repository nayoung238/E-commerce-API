package com.nayoung.accountservice.domain;

import com.nayoung.accountservice.client.OrderServiceClient;
import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.OrderResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final OrderServiceClient orderServiceClient;

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

        List<OrderResponse> responseList = new ArrayList<>();
        try {
            responseList = orderServiceClient.getOrders(id);
        } catch (FeignException e) {
            log.error(e.getMessage());
        }
        response.setOrders(responseList);
        return response;
    }
}
