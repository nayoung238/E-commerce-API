package com.ecommerce.itemservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class DiscountException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
