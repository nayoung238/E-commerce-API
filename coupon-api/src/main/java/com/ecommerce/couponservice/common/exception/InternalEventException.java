package com.ecommerce.couponservice.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class InternalEventException extends RuntimeException {

    private final ErrorCode errorCode;
}
