package com.nayoung.accountservice.domain;

import com.nayoung.accountservice.client.OrderServiceClient;
import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

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

        response.setOrders(orderServiceClient.getOrders(id));
        return response;
    }
}
