package com.ecommerce.apicomposer.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // feign client
    USER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "유저 서비스에 일시적으로 접근 불가능합니다."),

    // auth
    TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "토큰이 만료되었습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    JWT_PROCESSING_FAILED(HttpStatus.BAD_REQUEST, "JWT 처리 중 오류가 발생했습니다."),
    AUTHORIZATION_HEADER_MISSING(HttpStatus.UNAUTHORIZED, "Authorization 헤더가 없습니다."),
    UNAUTHORIZED_REQUEST(HttpStatus.FORBIDDEN, "인증되지 않은 요청입니다.");

    private final HttpStatus status;
    private final String message;
}
