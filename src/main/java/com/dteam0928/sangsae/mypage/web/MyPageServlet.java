package com.dteam0928.sangsae.mypage.web;

import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * 마이페이지 화면 (GET /mypage → WEB-INF/views/mypage/mypage.jsp).
 * JSP 는 프론트 담당. O-02 와 같은 ${form} 을 넣어 두므로 서버 렌더링도 가능하고,
 * 화면에서 /api/me/profile 을 fetch 해서 그려도 됨.
 */

@WebServlet(Routes.MYPAGE)
public class MyPageServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("form", app().onboardingService().getProfileForm(loginUser(req).getId()));
        forward(req, resp, Views.MYPAGE);
    }
}
