package com.ecommerce.couponservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomRedisException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}

