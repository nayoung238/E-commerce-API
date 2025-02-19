package com.ecommerce.accountservice.account.service;

import com.ecommerce.accountservice.account.dto.SignUpRequestDto;
import com.ecommerce.accountservice.account.repository.AccountRepository;
import com.ecommerce.accountservice.exception.CustomException;
import com.ecommerce.accountservice.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
public class AccountServiceIntegrationTest {

    @Autowired
    AccountService accountService;

    @Autowired
    AccountRepository accountRepository;

    @AfterEach
    void afterEach() {
        accountRepository.deleteAll();
    }

    @Test
    @DisplayName("[회원 가입 실패 테스트] loginId는 unique 제약 조건 설정")
    void duplicate_login_id_test () {
        // given
        SignUpRequestDto request = SignUpRequestDto.builder()
            .loginId("test-login-id")
            .password("test-password")
            .name("test-name")
            .build();

        accountService.createAccount(request);

        // when & then
        Assertions.assertThatThrownBy(() -> accountService.createAccount(request))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customException = (CustomException) ex;
                assertEquals(ErrorCode.DUPLICATE_LOGIN_ID, customException.getErrorCode());
                assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
                assertEquals("이미 사용 중인 로그인 아이디입니다.", customException.getErrorCode().getMessage());
            });
    }

    @Test
    @DisplayName("[회원 조회 실패 테스트] 존재하지 않는 계좌 조회 시 NOT_FOUND_ACCOUNT 에러 코드 예외 발생")
    void not_found_account_test () {
        // when & then
        Assertions.assertThatThrownBy(() -> accountService.findAccount(1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customException = (CustomException) ex;
                assertEquals(ErrorCode.NOT_FOUND_ACCOUNT, customException.getErrorCode());
                assertEquals(HttpStatus.NOT_FOUND, customException.getErrorCode().getHttpStatus());
                assertEquals("존재하지 않는 계정입니다.", customException.getErrorCode().getMessage());
            });
    }
}
