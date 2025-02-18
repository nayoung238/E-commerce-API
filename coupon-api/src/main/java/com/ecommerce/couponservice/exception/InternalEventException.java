package com.ecommerce.couponservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InternalEventException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
