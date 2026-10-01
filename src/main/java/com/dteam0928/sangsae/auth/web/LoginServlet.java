package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.auth.dto.LoginRequest;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** O-01 이메일 로그인 탭 */
@WebServlet(Routes.LOGIN)
public class LoginServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mode", "login");
        forward(req, resp, Views.EMAIL_AUTH);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginRequest form = new LoginRequest(param(req, "email"), param(req, "password"));
        try {
            LoginUser user = app().authService().login(form);
            LoginSession.login(req, user);
            redirect(req, resp, Routes.afterLogin(user));
        } catch (BusinessException e) {
            req.setAttribute("mode", "login");
            req.setAttribute("email", form.getEmail());   // 이메일은 다시 채워주고 비밀번호는 비운다
            forwardWithError(req, resp, Views.EMAIL_AUTH, e);
        }
    }
}
