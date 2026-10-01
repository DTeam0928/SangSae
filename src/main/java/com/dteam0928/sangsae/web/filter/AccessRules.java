package com.dteam0928.sangsae.web.filter;

import com.dteam0928.sangsae.common.web.Routes;

import java.util.List;
import java.util.Set;

/**
 * URL 별 접근 규칙. 새 화면이 생겨도 기본값은 "로그인 + 온보딩 완료 필요" 이므로 보통은 수정할 필요가 없다.
 * 비로그인 공개 페이지를 추가할 때만 여기에 경로를 넣는다.
 */
final class AccessRules {

    private AccessRules() {
    }

    /** 누구나 접근 (정적 파일 등) */
    static final List<String> PUBLIC_PREFIXES = List.of("/assets/", "/favicon.ico", "/error");

    /** 비로그인 전용. 로그인 상태로 오면 홈(또는 온보딩)으로 보낸다 */
    static final Set<String> GUEST_ONLY = Set.of(Routes.LANDING, Routes.LOGIN, Routes.SIGNUP);

    /** 로그인 여부와 무관하게 처리 */
    static final List<String> AUTH_FREE_PREFIXES = List.of(
            Routes.OAUTH_PREFIX + "/", "/api/auth/", Routes.LOGOUT);

    /** 로그인은 필요하지만 온보딩 미완료여도 접근 가능 */
    static final List<String> ONBOARDING_PREFIXES = List.of("/onboarding/");

    static boolean startsWithAny(String path, List<String> prefixes) {
        for (String p : prefixes) if (path.startsWith(p) || path.equals(p)) return true;
        return false;
    }
}
