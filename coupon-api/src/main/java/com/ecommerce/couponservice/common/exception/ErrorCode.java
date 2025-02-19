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
    ACCOUNT_NOT_IN_WAIT_QUEUE(HttpStatus.NOT_FOUND, "Account not found in the wait queue."),

    DUPLICATE_COUPON_NAME(HttpStatus.BAD_REQUEST, "Coupon name already exists"),
    NOT_VALID(HttpStatus.BAD_REQUEST, "value is not valid"),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "value is not acceptable"),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "Constraint were violated"),

    NOT_FOUND_INTERNAL_EVENT(HttpStatus.NOT_FOUND, "Internal event could not be found");

    private final HttpStatus httpStatus;
    private final String message;
}
