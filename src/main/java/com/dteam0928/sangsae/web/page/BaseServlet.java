package com.dteam0928.sangsae.web.page;

import com.dteam0928.sangsae.AppContext;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.web.FormErrors;
import com.dteam0928.sangsae.common.web.WebUtil;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** 모든 서블릿의 공통 부모. 자주 쓰는 동작을 짧게 호출할 수 있게 한다. */
public abstract class BaseServlet extends HttpServlet {

    protected AppContext app() {
        return AppContext.from(getServletContext());
    }

    protected LoginUser loginUser(HttpServletRequest req) {
        return LoginSession.current(req);
    }

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        WebUtil.moveFlashToRequest(req);
        req.getRequestDispatcher(view).forward(req, resp);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String route) throws IOException {
        WebUtil.redirect(req, resp, route);
    }

    /** 검증 실패 시: 에러를 request 에 담아 같은 화면을 다시 보여준다 */
    protected void forwardWithError(HttpServletRequest req, HttpServletResponse resp, String view,
                                    BusinessException e) throws ServletException, IOException {
        resp.setStatus(e.getErrorCode().getStatus() >= 500 ? 500 : 400);
        req.setAttribute(FormErrors.ATTR, FormErrors.of(e));
        forward(req, resp, view);
    }

    protected static String param(HttpServletRequest req, String name) {
        return req.getParameter(name);
    }
}
