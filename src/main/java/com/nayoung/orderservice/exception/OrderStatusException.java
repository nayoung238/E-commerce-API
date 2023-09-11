package com.nayoung.orderservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderStatusException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
