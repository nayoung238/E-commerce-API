package com.nayoung.accountservice.domain;

import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;

public interface AccountService {

    AccountResponse createAccount(SignUpRequest signUpRequest);
    AccountResponse getAccountById(Long id);
}
