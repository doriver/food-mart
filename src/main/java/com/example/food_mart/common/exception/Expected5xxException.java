package com.example.food_mart.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Expected5xxException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public Expected5xxException(String message) {
        this.httpStatus = HttpStatus.INTERNAL_SERVER_ERROR;
        this.message = message;
    }

    public Expected5xxException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public Expected5xxException(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
