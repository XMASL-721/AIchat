package com.example.project_aichat.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private int code;
    private String msg;
    private T data;

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "success", data);
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "success", null);
    }

    public static <T> ApiResponse<T> error(int code, String msg) {
        return new ApiResponse<>(code, msg, null);
    }

    public static <T> ApiResponse<T> error(String msg) {
        return new ApiResponse<>(400, msg, null);
    }

    // 常用业务码
    public static <T> ApiResponse<T> unauthorized() {
        return new ApiResponse<>(401, "未登录或 Token 已过期", null);
    }

    public static <T> ApiResponse<T> conflict(String msg) {
        return new ApiResponse<>(409, msg, null);
    }
}
