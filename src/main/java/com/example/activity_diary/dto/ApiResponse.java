package com.example.activity_diary.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return ok(data);
    }

    // Success with payload
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, null, data);
    }

    // success without payload, but with message
    public static <T> ApiResponse<T> successMessage(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Success with message only
    public static ApiResponse<Void> okMessage(String message) {
        return new ApiResponse<>(true, message, null);
    }

    // Failure
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null);
    }

    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>(false, message, data);
    }
}
