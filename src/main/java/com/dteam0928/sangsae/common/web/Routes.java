package com.dteam0928.sangsae.common.web;

import com.dteam0928.sangsae.auth.model.LoginUser;

/**
 * URL 경로 모음 (컨텍스트 경로 제외).
 * 서블릿 매핑·리다이렉트·필터가 모두 여기를 참조하므로 URL 을 바꿀 때 이 파일만 고치면 된다.
 * (서블릿 @WebServlet 매핑값은 애너테이션 제약상 상수를 그대로 참조한다)
 */
public final class Routes {

    private Routes() {
    }

    public static final String LANDING = "/";                 // O-00
    public static final String LOGIN = "/auth/login";         // O-01 로그인 탭
    public static final String SIGNUP = "/auth/signup";       // O-01 회원가입 탭
    public static final String LOGOUT = "/auth/logout";
    public static final String API_EMAIL_CHECK = "/api/auth/email-check";
    public static final String OAUTH_PREFIX = "/oauth";        // /oauth/{provider}, /oauth/{provider}/callback

    public static final String ONBOARDING_PROFILE = "/onboarding/profile";   // O-02
    public static final String ONBOARDING_LOCATION = "/onboarding/location"; // O-03

    public static final String HOME = "/home";                 // H-00
    public static final String API_ROUTINES = "/api/routines";               // R-00 탐색 API
    public static final String API_MY_PLACES = "/api/routines/my-places";    // R-00 장소 관리 API

    /** 로그인 직후 이동할 곳: 온보딩을 안 끝냈으면 O-02 */
    public static String afterLogin(LoginUser user) {
        return user.isOnboardingCompleted() ? HOME : ONBOARDING_PROFILE;
    }
}
