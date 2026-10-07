package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.common.web.WebUtil;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JSON API 서블릿 공통. 응답은 모두 {@link ApiResponse} 형식.
 * <ul>
 *   <li>BusinessException → ErrorCode 의 상태코드 + 실패 JSON</li>
 *   <li>그 밖의 예외 → 500 + INTERNAL_ERROR (서버 로그에 상세 기록)</li>
 * </ul>
 */
abstract class JsonApiServlet extends BaseServlet {

    private static final Logger log = Logger.getLogger(JsonApiServlet.class.getName());

    protected void respond(HttpServletResponse resp, Supplier<?> action) throws IOException {
        try {
            WebUtil.writeJson(resp, 200, ApiResponse.ok(null, action.get()));
        } catch (BusinessException e) {
            WebUtil.writeJson(resp, e.getErrorCode().getStatus(), ApiResponse.fail(e));
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "API 처리 실패", e);
            WebUtil.writeJson(resp, 500, ApiResponse.fail(ErrorCode.INTERNAL_ERROR));
        }
    }

    protected void notFound(HttpServletResponse resp) throws IOException {
        WebUtil.writeJson(resp, 404, ApiResponse.fail(ErrorCode.NOT_FOUND));
    }

    /** 로그인 사용자 id. AuthFilter 가 먼저 막지만 안전하게 한 번 더 확인 */
    protected long userId(HttpServletRequest req) {
        LoginUser user = loginUser(req);
        if (user == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return user.getId();
    }

    /** "/12/bookmark" → ["12", "bookmark"], 없으면 빈 목록 */
    protected static List<String> segments(HttpServletRequest req) {
        String info = req.getPathInfo();
        if (info == null || info.equals("/")) return List.of();
        return Arrays.stream(info.split("/")).filter(s -> !s.isEmpty()).toList();
    }

    protected static boolean isId(String s) {
        return s != null && s.matches("\\d{1,18}");
    }

    protected static Double doubleParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        if (v == null || v.isBlank()) return null;
        try {
            return Double.valueOf(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    protected static Long longParam(HttpServletRequest req, String name) {
        String v = req.getParameter(name);
        return isId(v == null ? null : v.trim()) ? Long.valueOf(v.trim()) : null;
    }

    protected static int intParam(HttpServletRequest req, String name, int defaultValue) {
        String v = req.getParameter(name);
        try {
            return v == null || v.isBlank() ? defaultValue : Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
