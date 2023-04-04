package com.nayoung.itemservice.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class LocationException extends RuntimeException {

    private final ExceptionCode exceptionCode;
}
