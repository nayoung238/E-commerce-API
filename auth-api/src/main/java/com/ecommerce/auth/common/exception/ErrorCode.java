package com.ecommerce.auth.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // entity
    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    NOT_FOUND_USER(HttpStatus.NOT_FOUND, "존재하지 않는 유저입니다."),

    // data
    DUPLICATE_LOGIN_ID(HttpStatus.BAD_REQUEST,"이미 사용 중인 로그인 아이디입니다."),

    // server
    TEMPORARILY_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "일시적으로 이용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
