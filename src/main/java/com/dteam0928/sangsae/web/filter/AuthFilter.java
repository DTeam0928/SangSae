package com.dteam0928.sangsae.web.filter;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 로그인/온보딩 접근 제어.
 * <ul>
 *   <li>비로그인으로 보호된 페이지 접근 → O-00 (AJAX 는 401 JSON)</li>
 *   <li>로그인 상태로 O-00/O-01 접근 → H-00 (O-00 정책: 세션 유효 시 자동 리다이렉트)</li>
 *   <li>온보딩 미완료 상태로 다른 페이지 접근 → O-02 (소셜 신규 가입자 강제 진입 정책)</li>
 * </ul>
 */
@WebFilter("/*")
public class AuthFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String path = WebUtil.pathOf(req);

        if (AccessRules.startsWithAny(path, AccessRules.PUBLIC_PREFIXES)
                || AccessRules.startsWithAny(path, AccessRules.AUTH_FREE_PREFIXES)) {
            chain.doFilter(req, resp);
            return;
        }

        LoginUser user = LoginSession.current(req);

        if (AccessRules.GUEST_ONLY.contains(path)) {
            if (user != null) {
                WebUtil.redirect(req, resp, Routes.afterLogin(user));
                return;
            }
            chain.doFilter(req, resp);
            return;
        }

        if (user == null) {
            if (WebUtil.isAjax(req)) {
                WebUtil.writeJson(resp, 401, ApiResponse.fail(ErrorCode.UNAUTHORIZED));
            } else {
                WebUtil.redirect(req, resp, Routes.LANDING);
            }
            return;
        }

        if (!user.isOnboardingCompleted() && !AccessRules.startsWithAny(path, AccessRules.ONBOARDING_PREFIXES)) {
            WebUtil.redirect(req, resp, Routes.ONBOARDING_PROFILE);
            return;
        }

        chain.doFilter(req, resp);
    }
}
