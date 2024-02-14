package com.ecommerce.accountservice.web;

import com.ecommerce.accountservice.domain.AccountService;
import com.ecommerce.accountservice.web.dto.AccountDto;
import com.ecommerce.accountservice.web.dto.SignUpDto;
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
    public ResponseEntity<?> signup(@RequestBody SignUpDto request) {
        AccountDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping(value = {"/{accountId}/{cursorOrderId}","/{accountId}" })
    public ResponseEntity<?> getAccountById(@PathVariable Long accountId, @PathVariable(required = false) Long cursorOrderId) {
        AccountDto response = accountService.getAccountById(accountId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
