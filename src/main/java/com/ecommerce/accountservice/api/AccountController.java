package com.ecommerce.accountservice.api;

import com.ecommerce.accountservice.api.dto.DetailedAccountDto;
import com.ecommerce.accountservice.domain.AccountService;
import com.ecommerce.accountservice.api.dto.SimpleAccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpRequestDto request) {
        SimpleAccountDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/accounts/simple/{accountId}")
    public ResponseEntity<?> findSimpleAccountInfo(@PathVariable Long accountId) {
        SimpleAccountDto response = accountService.findSimpleAccountInfoById(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/accounts/details/{accountId}")
    public ResponseEntity<?> findDetailedAccountInfo(@PathVariable Long accountId) {
        DetailedAccountDto response = accountService.findDetailedAccountInfoById(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
