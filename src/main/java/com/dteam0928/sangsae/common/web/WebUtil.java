package com.dteam0928.sangsae.common.web;

import com.dteam0928.sangsae.common.util.JsonUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class WebUtil {

    private WebUtil() {
    }

    /** 컨텍스트 경로를 뺀 요청 경로. 예: /sangsae/auth/login → /auth/login */
    public static String pathOf(HttpServletRequest req) {
        String uri = req.getRequestURI();
        String ctx = req.getContextPath();
        String path = uri.substring(ctx.length());
        return path.isEmpty() ? "/" : path;
    }

    /** fetch/XHR 요청인지. 프론트에서 헤더 X-Requested-With: XMLHttpRequest 를 붙이거나 Accept 에 json 을 넣는다 */
    public static boolean isAjax(HttpServletRequest req) {
        String xrw = req.getHeader("X-Requested-With");
        String accept = req.getHeader("Accept");
        return "XMLHttpRequest".equalsIgnoreCase(xrw)
                || (accept != null && accept.contains("application/json") && !accept.contains("text/html"));
    }

    public static void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Cache-Control", "no-store");
        resp.getWriter().write(JsonUtil.toJson(body));
    }

    public static void redirect(HttpServletRequest req, HttpServletResponse resp, String route) throws IOException {
        resp.sendRedirect(req.getContextPath() + route);
    }

    // ---- flash 메시지: 리다이렉트 너머로 한 번만 전달 ----

    public static void putFlash(HttpServletRequest req, String message) {
        req.getSession(true).setAttribute(SessionKeys.FLASH_MESSAGE, message);
    }

    /** 세션의 flash 메시지를 꺼내 request 속성으로 옮긴다 (JSP 에서 ${flashMessage}) */
    public static void moveFlashToRequest(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return;
        Object msg = session.getAttribute(SessionKeys.FLASH_MESSAGE);
        if (msg != null) {
            session.removeAttribute(SessionKeys.FLASH_MESSAGE);
            req.setAttribute(SessionKeys.FLASH_MESSAGE, msg);
        }
    }
}
