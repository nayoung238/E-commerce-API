package com.ecommerce.couponservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ExceptionCode {

    NOT_FOUND(HttpStatus.NOT_FOUND, "not found"),
    NOT_FOUND_COUPON(HttpStatus.NOT_FOUND, "Coupon could not be found"),
    DUPLICATE_COUPON_NAME(HttpStatus.BAD_REQUEST, "Coupon name already exists"),
    NOT_VALID(HttpStatus.BAD_REQUEST, "value is not valid"),
    ILLEGAL_ARGUMENT(HttpStatus.BAD_REQUEST, "value is not acceptable"),
    CONSTRAINT_VIOLATION(HttpStatus.BAD_REQUEST, "Constraint were violated");

    private final HttpStatus httpStatus;
    private final String message;
}
