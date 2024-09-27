package com.ecommerce.couponservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomRedisException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}

