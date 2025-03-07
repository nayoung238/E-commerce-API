package com.ecommerce.auth.user.service;

import com.ecommerce.auth.user.dto.request.SignUpRequest;
import com.ecommerce.auth.user.repository.UserRepository;
import com.ecommerce.auth.common.exception.CustomException;
import com.ecommerce.auth.common.exception.ErrorCode;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
public class UserServiceIntegrationTest {

    @Autowired
    UserService userService;

    @Autowired
    UserRepository userRepository;

    @AfterEach
    void afterEach() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("[회원 가입 실패 테스트] loginId는 unique 제약 조건 설정")
    void duplicate_login_id_test () {
        // given
        SignUpRequest request = SignUpRequest.builder()
            .loginId("test-login-id")
            .password("test-password")
            .name("test-name")
            .build();

        userService.createUser(request);

        // when & then
        Assertions.assertThatThrownBy(() -> userService.createUser(request))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customException = (CustomException) ex;
                assertEquals(ErrorCode.DUPLICATE_LOGIN_ID, customException.getErrorCode());
                assertEquals(HttpStatus.BAD_REQUEST, customException.getErrorCode().getHttpStatus());
                assertEquals("이미 사용 중인 로그인 아이디입니다.", customException.getErrorCode().getMessage());
            });
    }

    @Test
    @DisplayName("[회원 조회 실패 테스트] 존재하지 않는 유저 조회 시 NOT_FOUND_USER 에러 코드 예외 발생")
    void not_found_user_test () {
        // when & then
        Assertions.assertThatThrownBy(() -> userService.findUser(1L))
            .isInstanceOf(CustomException.class)
            .satisfies(ex -> {
                CustomException customException = (CustomException) ex;
                assertEquals(ErrorCode.NOT_FOUND_USER, customException.getErrorCode());
                assertEquals(HttpStatus.NOT_FOUND, customException.getErrorCode().getHttpStatus());
                assertEquals("존재하지 않는 유저입니다.", customException.getErrorCode().getMessage());
            });
    }
}
