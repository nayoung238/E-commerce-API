package com.ecommerce.accountservice.account.api;

import com.ecommerce.accountservice.account.service.AccountService;
import com.ecommerce.accountservice.account.dto.AccountResponseDto;
import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Accounts", description = "계정 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "회원 가입", description = "바디에 {loginId, password, name}을 json 형식으로 보내주세요.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "계정 생성 성공", content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "제약 조건 위반", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody @Valid SignUpRequestDto request) {
        AccountResponseDto response = accountService.createAccount(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "계정 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "계정 조회 성공", content = @Content(schema = @Schema(implementation = AccountResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 계정", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{accountId}")
    public ResponseEntity<?> findAccount(@PathVariable Long accountId) {
        AccountResponseDto response = accountService.findAccount(accountId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
