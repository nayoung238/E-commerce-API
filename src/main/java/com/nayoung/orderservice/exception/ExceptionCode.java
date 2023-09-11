package com.nayoung.orderservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    NOT_FOUND_ORDER(HttpStatus.BAD_REQUEST, "존재하지 않는 주문입니다."),
    NO_MATCHING_ORDER_STATUS(HttpStatus.NOT_FOUND, "일치하는 주문 상태가 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
