package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.SessionKeys;
import com.dteam0928.sangsae.common.web.WebUtil;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

/**
 * O-01 [중복확인] AJAX.
 * <pre>
 * POST /api/auth/email-check   (form: email=...)
 * 200 {"success":true,"message":"사용 가능한 이메일입니다.","data":{"email":"a@b.com"}}
 * 409 {"success":false,"code":"EMAIL_DUPLICATED","field":"email","message":"이미 가입된 이메일입니다."}
 * 400 {"success":false,"code":"EMAIL_INVALID", ...}
 * </pre>
 * 통과한 이메일은 세션에 저장해 두고, 가입 시 같은 이메일인지 서버에서 다시 확인한다.
 */
@WebServlet(Routes.API_EMAIL_CHECK)
public class EmailCheckApiServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            String email = app().authService().checkEmailAvailable(param(req, "email"));
            req.getSession(true).setAttribute(SessionKeys.CHECKED_EMAIL, email);
            WebUtil.writeJson(resp, 200, ApiResponse.ok("사용 가능한 이메일입니다.", Map.of("email", email)));
        } catch (BusinessException e) {
            if (req.getSession(false) != null) req.getSession().removeAttribute(SessionKeys.CHECKED_EMAIL);
            WebUtil.writeJson(resp, e.getErrorCode().getStatus(), ApiResponse.fail(e));
        }
    }
}
