package com.example.food_mart.common.exception;

import com.example.food_mart.common.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
/*
    API에서
    상태코드 4xx, 5xx 응답을 담당
 */
    @ExceptionHandler(Expected4xxException.class)
    public ResponseEntity<ApiResponse> handleExpected4xxException(Expected4xxException ex) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST) // 응답 헤더에는 400 코드가 가되, 실제 상태 코드는 ex에 들어있는 상태코드
                .body(ApiResponse.error(ex.getHttpStatus(), ex.getMessage()));
    }

    // API 데이터 validation
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        BindingResult bindingResult = ex.getBindingResult();
        StringBuilder builder = new StringBuilder();
        for (FieldError fieldError : bindingResult.getFieldErrors()) {
            builder.append("[");
            builder.append(fieldError.getField());
            builder.append("](은)는 ");
            builder.append(fieldError.getDefaultMessage());
            builder.append(" 입력된 값: [");
            builder.append(fieldError.getRejectedValue());
            builder.append("]");
            builder.append("\n");
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST, builder.toString()));
    }

    @ExceptionHandler(Expected5xxException.class)
    public ResponseEntity<ApiResponse> handleExpected5xxException(Expected5xxException ex) {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 응답 헤더에는 500 코드가 가되, 실제 상태 코드는 ex에 들어있는 상태코드
                .body(ApiResponse.error(ex.getHttpStatus(), ex.getMessage()));
    }
/*
    예상치 못한 에러가 발생한 경우
 */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> handleUnExpectedException(Exception ex) {
        log.error("에러 발생 :", ex);

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."));
    }
}
