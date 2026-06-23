package com.rc.readcompass.exception;

import com.rc.readcompass.exception.base.CustomException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {

        ErrorResponse response = ErrorResponse.builder()
                .status(e.getErrorCode().getStatus().value())
                .message(e.getErrorCode().getMessage())
                .details(e.getDetails())
                .build();

        return ResponseEntity
                .status(e.getErrorCode().getStatus())
                .body(response);
    }
}