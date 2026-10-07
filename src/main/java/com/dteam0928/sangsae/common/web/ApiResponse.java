package com.dteam0928.sangsae.common.web;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;

/**
 * AJAX 공통 응답 형식.
 * <pre>
 * 성공: {"success":true,  "message":"사용 가능한 이메일입니다.", "data":{...}}
 * 실패: {"success":false, "code":"EMAIL_DUPLICATED", "field":"email", "message":"이미 가입된 이메일입니다."}
 * </pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final String code;
    private final String field;
    private final String message;
    private final T data;

    private ApiResponse(boolean success, String code, String field, String message, T data) {
        this.success = success;
        this.code = code;
        this.field = field;
        this.message = message;
        this.data = data;
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, null, null, message, data);
    }

    public static <T> ApiResponse<T> ok(T data) {
        return ok(null, data);
    }

    public static ApiResponse<Void> ok(String message) {
        return ok(message, null);
    }

    public static ApiResponse<Void> fail(String message) {
        return new ApiResponse<>(false, null, null, message, null);
    }

    public static ApiResponse<Void> fail(BusinessException e) {
        return new ApiResponse<>(false, e.getErrorCode().name(), e.getField(), e.getMessage(), null);
    }

    public static ApiResponse<Void> fail(ErrorCode code) {
        return fail(new BusinessException(code));
    }

    public boolean isSuccess() { return success; }
    public String getCode() { return code; }
    public String getField() { return field; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
