package com.ecommerce.accountservice.web;

import com.ecommerce.accountservice.domain.AccountService;
import com.ecommerce.accountservice.web.dto.AccountDto;
import com.ecommerce.accountservice.web.dto.OrderListDto;
import com.ecommerce.accountservice.web.dto.SignUpDto;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
public class AccountController {

    private final AccountService accountService;
    private final Environment env;

    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody SignUpDto request) {
        AccountDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{userId}")
    public ResponseEntity<?> getAccount(@PathVariable Long userId) {
        AccountDto response = accountService.getAccountById(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping(value = {"/order-list/{userId}/{cursorOrderId}", "/order-list/{userId}"})
    public ResponseEntity<?> getOrderList(@PathVariable Long userId, @PathVariable(required = false) Long cursorOrderId) {
        OrderListDto response = accountService.getOrderList(userId, cursorOrderId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }
}
