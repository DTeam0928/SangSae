package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.web.SessionKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/** 로그인 세션 관리. 세션을 다루는 코드는 이 클래스로만 모은다. */
public final class LoginSession {

    /** 세션 유효 시간(초). web.xml 의 session-timeout 과 별개로 로그인 시점에 적용 */
    private static final int MAX_INACTIVE_SECONDS = -1;

    private LoginSession() {
    }

    public static void login(HttpServletRequest req, LoginUser user) {
        HttpSession session = req.getSession(true);
        req.changeSessionId();                       // 세션 고정 공격 방지
        session.removeAttribute(SessionKeys.CHECKED_EMAIL);
        session.setAttribute(SessionKeys.LOGIN_USER, user);
        session.setMaxInactiveInterval(MAX_INACTIVE_SECONDS);
    }

    public static LoginUser current(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (LoginUser) session.getAttribute(SessionKeys.LOGIN_USER);
    }

    public static boolean isLoggedIn(HttpServletRequest req) {
        return current(req) != null;
    }

    /** 닉네임 변경, 온보딩 완료 등 세션 정보 갱신 */
    public static void update(HttpServletRequest req, LoginUser user) {
        req.getSession(true).setAttribute(SessionKeys.LOGIN_USER, user);
    }

    public static void logout(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session != null) session.invalidate();
    }
}
