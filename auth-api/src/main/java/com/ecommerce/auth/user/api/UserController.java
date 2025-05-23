package com.ecommerce.auth.user.api;

import com.ecommerce.auth.auth.entity.UserPrincipal;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import com.ecommerce.auth.user.service.UserService;
import com.ecommerce.auth.user.dto.response.UserResponse;
import com.ecommerce.auth.user.dto.request.SignUpRequest;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Users", description = "유저 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final UserService userService;

    @Operation(summary = "회원 가입", description = "바디에 {loginId, password, name}을 json 형식으로 보내주세요.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "사용자 생성 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "제약 조건 위반", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> signup(@RequestBody @Valid SignUpRequest request) {
        UserResponse response = userService.createUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "사용자 조회")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "사용자 조회 성공", content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "400", description = "존재하지 않는 계정", content = @Content(schema = @Schema(implementation = String.class)))
    })
    @GetMapping("/{userId}")
    public ResponseEntity<?> findUser(@PathVariable Long userId,
                                      @AuthenticationPrincipal UserPrincipal userPrincipal) {
        if (!userPrincipal.getId().equals(userId)) {
            throw new CustomException(ErrorCode.FORBIDDEN);
        }

        UserResponse response = userService.findUser(userId);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        log.info("health check...");
        return ResponseEntity.ok("health check...");
    }
}
