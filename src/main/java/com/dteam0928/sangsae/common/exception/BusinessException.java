package com.dteam0928.sangsae.common.exception;

import java.text.MessageFormat;

/** 사용자에게 안내해야 하는 예상된 오류 (검증 실패, 중복 등) */
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

    public BusinessException(ErrorCode errorCode, Object... args) {
        super(args.length == 0 ? errorCode.getMessage() : MessageFormat.format(errorCode.getMessage(), args));
        this.errorCode = errorCode;
    }

    public BusinessException(ErrorCode errorCode, Throwable cause, Object... args) {
        this(errorCode, args);
        initCause(cause);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    /** 인라인 에러를 붙일 필드. 없으면 "global" */
    public String getField() {
        return errorCode.getField() == null ? "global" : errorCode.getField();
    }
}
