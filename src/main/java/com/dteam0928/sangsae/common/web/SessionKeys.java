package com.dteam0928.sangsae.common.web;

/** 세션 속성 이름 */
public final class SessionKeys {

    private SessionKeys() {
    }

    /** 로그인 사용자 ({@link com.dteam0928.sangsae.auth.model.LoginUser}) */
    public static final String LOGIN_USER = "loginUser";
    /** 회원가입 전 중복확인을 통과한 이메일 */
    public static final String CHECKED_EMAIL = "checkedEmail";
    /** 리다이렉트 후 1회 보여줄 메시지 */
    public static final String FLASH_MESSAGE = "flashMessage";
}
