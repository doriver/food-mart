package com.example.food_mart.common;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class ApiResponse<T> {
    private final int status;
    private final T data;
    private final String errorMessage;

    // 성공 응답 - 200 상태코드의 경우 상태코드를 파라미터로 전달 받지 않음
    // 개발 시 Controller 응답에 직접 사용
    public static ApiResponse<String> success() {
        return new ApiResponse<>(HttpStatus.OK.value(), "success", null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), data, null);
    }

    public static <T> ApiResponse<T> success(T data, HttpStatus status) {
        return new ApiResponse<>(status.value(), data, null);
    }

    // 실패 응답
    // GlobalExceptionHandler에서 사용됨
    public static <T> ApiResponse<T> error(HttpStatus httpStatus, String errorMessage) {
        return new ApiResponse<>(httpStatus.value(), null, errorMessage);
    }
}
