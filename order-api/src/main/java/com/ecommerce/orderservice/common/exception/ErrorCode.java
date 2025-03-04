package com.ecommerce.orderservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않습니다."),
    NOT_FOUND_ORDER(HttpStatus.NOT_FOUND, "주문이 존재하지 않습니다."),
    NO_MATCHING_ORDER_STATUS(HttpStatus.NOT_FOUND, "일치하는 주문 상태가 없습니다."),
    NOT_FOUND_ORDER_CREATION_INTERNAL_EVENT(HttpStatus.NOT_FOUND, "주문 생성 내부 이벤트가 존재하지 않습니다."),

    NOT_VALID(HttpStatus.BAD_REQUEST, "유효하지 않은 값입니다."),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "제약조건 위반"),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
