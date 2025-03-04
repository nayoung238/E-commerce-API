package com.ecommerce.auth.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // user
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "비밀번호가 일치하지 않습니다."),

    // JWT
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    REFRESH_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 만료되었습니다."),
    REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 없습니다."),
    JWT_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "JWT 처리 중 오류가 발생했습니다."),
    MISSING_USER_ID(HttpStatus.BAD_REQUEST, "토큰 생성 시 userId 값은 필수입니다."),
    MISSING_ROLE(HttpStatus.BAD_REQUEST, "토큰 생성 시 role 값은 필수입니다."),

    // data
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST,"이미 사용 중인 로그인 아이디입니다."),

    // server
    TEMPORARILY_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "일시적으로 이용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
