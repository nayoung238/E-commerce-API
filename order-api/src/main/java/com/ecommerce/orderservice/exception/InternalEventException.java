package com.ecommerce.orderservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InternalEventException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
