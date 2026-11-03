package com.dteam0928.sangsae.web.page;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.home.HomeModel;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * H-00 홈 종합 대시보드 서블릿.
 * 사용자의 경험치, 레벨, 오늘 퀘스트/루틴 진행 통계를 조회하여 home.jsp 로 모델 전달.
 */
@WebServlet(Routes.HOME)
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginUser loginUser = LoginSession.current(req);
        Long userId = (loginUser != null) ? loginUser.getId() : null;
        String nickname = (loginUser != null) ? loginUser.getNickname() : null;

        HomeModel homeModel = app().homeDashboardService().buildDashboard(userId, nickname);
        req.setAttribute("homeModel", homeModel);

        forward(req, resp, Views.HOME);
    }
}
