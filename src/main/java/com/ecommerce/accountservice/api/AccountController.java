package com.ecommerce.accountservice.api;

import com.ecommerce.accountservice.domain.AccountService;
import com.ecommerce.accountservice.api.dto.AccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpRequestDto request) {
        AccountDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAccount(@PathVariable Long userId) {
        AccountDto response = accountService.getAccountById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
