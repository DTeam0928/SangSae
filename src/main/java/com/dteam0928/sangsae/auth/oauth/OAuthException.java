package com.dteam0928.sangsae.auth.oauth;

/** 제공자와의 통신 실패, 응답 형식 오류 등 (사용자 입력 문제가 아닌 오류) */
public class OAuthException extends RuntimeException {
    public OAuthException(String message) {
        super(message);
    }

    public OAuthException(String message, Throwable cause) {
        super(message, cause);
    }
}
