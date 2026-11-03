package com.dteam0928.sangsae.user.exp.servlet;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import com.dteam0928.sangsae.user.exp.LevelInfo;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

// 현재 로그인한 사용자의 경험치, 레벨, 칭호 및 프로그레스바 정보를 제공하는 RESTful API 서블릿
@WebServlet("/api/user/experience")
public class UserExperienceApiServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginUser loginUser = LoginSession.current(req);
        if (loginUser == null) {
            resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write(JsonUtil.toJson(ApiResponse.fail("로그인이 필요합니다.")));
            return;
        }

        ExperienceService expService = app().experienceService();
        LevelInfo levelInfo = expService.getUserLevelInfo(loginUser.getId());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("userId", loginUser.getId());
        data.put("level", levelInfo.level());
        data.put("title", levelInfo.title());
        data.put("totalXp", levelInfo.totalXp());
        data.put("currentLevelXp", levelInfo.currentLevelXp());
        data.put("nextLevelRemainingXp", levelInfo.nextLevelRemainingXp());
        data.put("progressPercent", levelInfo.progressPercent());
        data.put("isMaxLevel", levelInfo.isMaxLevel());

        resp.setStatus(HttpServletResponse.SC_OK);
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(JsonUtil.toJson(ApiResponse.ok(data)));
    }
}
