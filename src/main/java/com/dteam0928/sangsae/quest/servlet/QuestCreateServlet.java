package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//신규 공개 퀘스트 생성 로직
@WebServlet("/quests/create")
public class QuestCreateServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        // 로그인 확인
        LoginUser loginUser = LoginSession.current(request);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }

        try {
            Long creatorId = loginUser.getId();
            String title = request.getParameter("title");
            String description = request.getParameter("description");
            String category = request.getParameter("category");
            String authType = request.getParameter("authType");
            String visibility = request.getParameter("visibility");
            String placeIdParam = request.getParameter("placeId");
            Long placeId = (placeIdParam == null || placeIdParam.isBlank())
                    ? null
                    : Long.parseLong(placeIdParam);
            int estimatedMinutes = Integer.parseInt(request.getParameter("estimatedMinutes"));
            int rewardXp = Integer.parseInt(request.getParameter("rewardXp"));

            // 기존 Service의 생성 메서드 호출
            QuestService questService = app().questService();
            QuestTemplate createdQuest = questService.createQuest(
                    creatorId,
                    placeId,
                    title,
                    description,
                    category,
                    estimatedMinutes,
                    rewardXp,
                    authType,
                    visibility
            );

            // 생성 성공 응답
            response.setStatus(HttpServletResponse.SC_CREATED);

            response.getWriter().write(
                    com.dteam0928.sangsae.common.util.JsonUtil.toJson(
                            com.dteam0928.sangsae.common.web.ApiResponse.ok(
                                    createdQuest
                            )
                    )
            );

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                    com.dteam0928.sangsae.common.util.JsonUtil.toJson(
                            com.dteam0928.sangsae.common.web.ApiResponse.fail(e.getMessage())));
        }
    }
}
