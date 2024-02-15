package com.ecommerce.accountservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class AccountException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
