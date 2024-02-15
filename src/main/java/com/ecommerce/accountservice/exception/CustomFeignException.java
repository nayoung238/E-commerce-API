package com.ecommerce.accountservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class CustomFeignException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
