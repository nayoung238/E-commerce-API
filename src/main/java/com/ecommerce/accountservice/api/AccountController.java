package com.ecommerce.accountservice.api;

import com.ecommerce.accountservice.api.dto.DetailedAccountDto;
import com.ecommerce.accountservice.domain.service.AccountService;
import com.ecommerce.accountservice.api.dto.SimpleAccountDto;
import com.ecommerce.accountservice.api.dto.SignUpRequestDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpRequestDto request) {
        SimpleAccountDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/simple/{accountId}")
    public ResponseEntity<?> findSimpleAccountInfo(@PathVariable Long accountId) {
        SimpleAccountDto response = accountService.findSimpleAccountInfoById(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/details/{accountId}")
    public ResponseEntity<?> findDetailedAccountInfo(@PathVariable Long accountId) {
        DetailedAccountDto response = accountService.findDetailedAccountInfoById(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
