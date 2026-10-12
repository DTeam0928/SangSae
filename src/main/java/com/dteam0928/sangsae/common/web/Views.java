package com.dteam0928.sangsae.common.web;

/** JSP 경로 모음. JSP 는 WEB-INF 아래에 두어 URL 로 직접 접근할 수 없게 한다. */
public final class Views {

    private Views() {
    }

    private static final String PREFIX = "/WEB-INF/views/";

    public static final String LANDING = PREFIX + "auth/landing.jsp";               // O-00
    public static final String EMAIL_AUTH = PREFIX + "auth/email-auth.jsp";         // O-01
    public static final String ONBOARDING_PROFILE = PREFIX + "onboarding/profile.jsp";   // O-02
    public static final String ONBOARDING_LOCATION = PREFIX + "onboarding/location.jsp"; // O-03
    public static final String HOME = PREFIX + "home/home.jsp";
    public static final String QUEST = PREFIX + "quest/quest.jsp";
    public static final String ROUTINE = PREFIX + "routine/routine.jsp";
    public static final String MYPAGE = PREFIX + "mypage/mypage.jsp";
}
