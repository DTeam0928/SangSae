package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.auth.dto.SignupRequest;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.SessionKeys;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

/** O-01 회원가입 탭. 성공 시 자동 로그인 후 O-02 로 이동 */
@WebServlet(Routes.SIGNUP)
public class SignupServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("mode", "signup");
        forward(req, resp, Views.EMAIL_AUTH);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        SignupRequest form = new SignupRequest(param(req, "email"), param(req, "password"),
                param(req, "passwordConfirm"));
        HttpSession session = req.getSession(false);
        String checkedEmail = session == null ? null : (String) session.getAttribute(SessionKeys.CHECKED_EMAIL);
        try {
            LoginUser user = app().authService().signup(form, checkedEmail);
            LoginSession.login(req, user);
            redirect(req, resp, Routes.ONBOARDING_PROFILE);
        } catch (BusinessException e) {
            req.setAttribute("mode", "signup");
            req.setAttribute("email", form.getEmail());
            // 이미 중복확인을 통과한 이메일이면 화면에서 통과 상태를 유지
            req.setAttribute("emailChecked", checkedEmail != null
                    && checkedEmail.equalsIgnoreCase(String.valueOf(form.getEmail()).trim()));
            forwardWithError(req, resp, Views.EMAIL_AUTH, e);
        }
    }
}
