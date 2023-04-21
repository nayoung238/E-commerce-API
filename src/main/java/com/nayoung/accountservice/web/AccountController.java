package com.nayoung.accountservice.web;

import com.nayoung.accountservice.domain.AccountService;
import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final Environment env;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        AccountResponse response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{accountId}/{cursorOrderId}","/{accountId}" })
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId, @PathVariable(required = false) Long cursorOrderId) {
        AccountResponse response = accountService.getAccountById(accountId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health-check")
    public String healthCheck() {
        return String.format("It's Working in Account Service"
                + ", port(local.server.port)= " + env.getProperty("server.port")
                + ", account id=" + env.getProperty("account.id")
                + ", account password=" + env.getProperty("account.password"));
    }

}
