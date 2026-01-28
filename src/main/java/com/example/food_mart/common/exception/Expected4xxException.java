package com.example.food_mart.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class Expected4xxException extends RuntimeException {
    private final HttpStatus httpStatus;
    private final String message;

    public Expected4xxException(String message) {
        this.httpStatus = HttpStatus.BAD_REQUEST;
        this.message = message;
    }

    public Expected4xxException(HttpStatus httpStatus, String message) {
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public Expected4xxException(ErrorCode errorCode) {
        this.httpStatus = errorCode.getHttpStatus();
        this.message = errorCode.getMessage();
    }
}
