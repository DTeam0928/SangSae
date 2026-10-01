package com.dteam0928.sangsae.web.page;

import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/** H-00 홈 (임시). 홈 담당자가 실제 구현으로 교체한다. */
@WebServlet(Routes.HOME)
public class HomeServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forward(req, resp, Views.HOME);
    }
}
