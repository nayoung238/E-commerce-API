package com.nayoung.accountservice.web;

import com.nayoung.accountservice.domain.AccountService;
import com.nayoung.accountservice.web.dto.AccountResponse;
import com.nayoung.accountservice.web.dto.SignUpRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/")
public class AccountController {

    private final AccountService accountService;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpRequest request) {
        AccountResponse accountDto = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(accountDto);
    }
}
