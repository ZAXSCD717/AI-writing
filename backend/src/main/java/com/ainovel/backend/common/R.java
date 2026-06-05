package com.ainovel.backend.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Unified API response wrapper
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class R<T> {

    private int code;
    private String message;
    private T data;

    private static final int SUCCESS_CODE = 200;
    private static final int ERROR_CODE = 500;

    public static <T> R<T> ok(T data) {
        return new R<>(SUCCESS_CODE, "success", data);
    }

    public static <T> R<T> ok() {
        return new R<>(SUCCESS_CODE, "success", null);
    }

    public static <T> R<T> error(String message) {
        return new R<>(ERROR_CODE, message, null);
    }

    public static <T> R<T> error(int code, String message) {
        return new R<>(code, message, null);
    }
}
