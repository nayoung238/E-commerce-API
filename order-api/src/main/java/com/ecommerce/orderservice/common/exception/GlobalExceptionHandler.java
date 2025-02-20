package com.ecommerce.orderservice.common.exception;

import com.ecommerce.orderservice.common.exception.response.ExceptionResponse;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .code(String.valueOf(ErrorCode.NOT_VALID))
                .message(Objects.requireNonNull(ex.getBindingResult().getFieldError()).getDefaultMessage())
                .build();

        return new ResponseEntity<>(exceptionResponse, ErrorCode.NOT_VALID.getHttpStatus());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolationException(ConstraintViolationException e) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .code(String.valueOf(ErrorCode.CONSTRAINT_VIOLATION))
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(exceptionResponse, ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus());
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<?> handleEntityNotFoundException(EntityNotFoundException e) {
        ExceptionResponse exceptionResponse = ExceptionResponse.builder()
                .code(String.valueOf(ErrorCode.NOT_FOUND))
                .message(e.getMessage())
                .build();

        return new ResponseEntity<>(exceptionResponse, ErrorCode.NOT_FOUND.getHttpStatus());
    }

    private ResponseEntity<Object> handleExceptionInternal(ErrorCode errorCode) {
        return ResponseEntity.status(errorCode.getHttpStatus())
                .body(createExceptionResponse(errorCode));
    }

    private ExceptionResponse createExceptionResponse(ErrorCode errorCode) {
        return ExceptionResponse.builder()
                .code(errorCode.name())
                .message(errorCode.getMessage())
                .build();
    }
}
