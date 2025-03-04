package com.ecommerce.couponservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "not found"),
    NOT_FOUND_COUPON(HttpStatus.NOT_FOUND, "Coupon could not be found"),
    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "All coupons have been redeemed."),

    WAIT_QUEUE_NOT_FOUND(HttpStatus.NOT_FOUND, "The wait queue does not exist"),
    USER_NOT_IN_WAIT_QUEUE(HttpStatus.NOT_FOUND, "User not found in the wait queue."),

    DUPLICATE_COUPON_NAME(HttpStatus.BAD_REQUEST, "Coupon name already exists"),
    NOT_VALID(HttpStatus.BAD_REQUEST, "value is not valid"),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "value is not acceptable"),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "Constraint were violated"),

    NOT_FOUND_INTERNAL_EVENT(HttpStatus.NOT_FOUND, "Internal event could not be found"),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),
    ACCESS_TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, "액세스 토큰이 만료되었습니다."),
    ACCESS_TOKEN_FORMAT_INVALID(HttpStatus.BAD_REQUEST, "엑세스 토큰 형식이 잘못되었습니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String message;
}
