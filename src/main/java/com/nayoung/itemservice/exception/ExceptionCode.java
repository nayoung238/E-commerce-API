package com.nayoung.itemservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    NOT_FOUND_ITEM(HttpStatus.BAD_REQUEST, "존재하지 않는 상품입니다."),
    INSUFFICIENT_STOCK_EXCEPTION(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),
    NO_MATCHING_DISCOUNT_CODE(HttpStatus.BAD_REQUEST, "매칭되는 할인이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
