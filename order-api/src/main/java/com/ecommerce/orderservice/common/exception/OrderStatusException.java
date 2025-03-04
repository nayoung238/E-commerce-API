package com.ecommerce.orderservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class OrderStatusException extends RuntimeException {

    private final ErrorCode errorCode;
}
