package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** 로그아웃. 다른 사이트가 링크 하나로 로그아웃시키지 못하도록 POST 만 받는다 */
@WebServlet(Routes.LOGOUT)
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginSession.logout(req);
        redirect(req, resp, Routes.LANDING);
    }
}
