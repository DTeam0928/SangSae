package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.stream.Collectors;

/**
 * O-00 온보딩(소셜 로그인) 랜딩.
 * 매핑 "" 은 컨텍스트 루트(/sangsae/)에만 정확히 매칭된다.
 * 이미 로그인한 사용자는 AuthFilter 가 H-00 으로 보낸다.
 */
@WebServlet("")
public class LandingServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        // JSP 에서 ${enabledProviders.contains('google')} 로 버튼 활성화 여부 판단
        req.setAttribute("enabledProviders", app().oAuthProviderRegistry().enabledProviders().stream()
                .map(AuthProvider::getPathName).collect(Collectors.toSet()));
        forward(req, resp, Views.LANDING);
    }
}
