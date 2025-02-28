package com.ecommerce.apicomposer.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Open Feign
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "유저 서비스에 일시적으로 접근 불가능합니다.");

    private final HttpStatus status;
    private final String message;
}
