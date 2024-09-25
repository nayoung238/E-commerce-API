package com.ecommerce.accountservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "Not Found"),
    NOT_FOUND_ACCOUNT(HttpStatus.NOT_FOUND, "Account not found"),
    TEMPORARILY_UNAVAILABLE(HttpStatus.INTERNAL_SERVER_ERROR, "일시적으로 이용할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
