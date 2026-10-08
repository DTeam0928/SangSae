package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

//사용자가 공개 퀘스트에서 등록한 퀘스트들을 조회
//UserQuest 조회
@WebServlet("/user-quests")
public class UserQuestServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	LoginUser loginUser = LoginSession.current(request);
    	if (loginUser == null) {
    	    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
    	    return;
    	}
    	QuestService questService = app().questService();
    	Long userId = loginUser.getId();
        String tripIdParam = request.getParameter("tripId");
        Long tripId = (tripIdParam == null || tripIdParam.isBlank())
                ? null
                : Long.parseLong(tripIdParam);
        String dateParam = request.getParameter("date");
        LocalDate scheduledDate = (dateParam != null && !dateParam.isBlank())
                ? LocalDate.parse(dateParam.trim())
                : LocalDate.now();
        List<UserQuest> userQuests = questService.getUserQuestsByDate(
                        userId,
                        tripId,
                        scheduledDate
                );

        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains("application/json");
        if (wantsJson) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.ok(userQuests)));
            return;
        }

        request.setAttribute("userQuests", userQuests);
        request.getRequestDispatcher("/WEB-INF/views/quest/quest.jsp").forward(request, response);
    }
    
    //삭제버튼을 누를 시
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        QuestService questService = app().questService();

        // 1. 로그인 사용자 확인
        LoginUser loginUser = LoginSession.current(request);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }
        Long userId = loginUser.getId();
        String accept = request.getHeader("Accept");
        boolean wantsJson = accept != null && accept.contains("application/json");

        // 2. 삭제할 사용자 퀘스트 ID 확인
        String userQuestIdParam = request.getParameter("userQuestId");
        if (userQuestIdParam == null || userQuestIdParam.isBlank()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "삭제할 퀘스트가 지정되지 않았습니다.");
            return;
        }
        Long userQuestId;
        try {
            userQuestId = Long.parseLong(userQuestIdParam);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 퀘스트 ID입니다.");
            return;
        }

        // 3. 일정 삭제
        try {
            boolean deleted = questService.deleteUserQuest(userQuestId, userId);
            if (!deleted) {
                if (wantsJson) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.fail("삭제할 퀘스트를 찾을 수 없습니다.")));
                    return;
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "삭제할 퀘스트를 찾을 수 없습니다.");
                return;
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (wantsJson) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.fail(e.getMessage())));
                return;
            }
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
            return;
        }

        if (wantsJson) {
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.ok("삭제 완료")));
            return;
        }

        // 4. 삭제 후 일정 목록으로 이동
        String tripId = request.getParameter("tripId");
        String date = request.getParameter("date");
        String redirectUrl = request.getContextPath() + "/user-quests?date=" + (date != null ? date : LocalDate.now().toString());
        if (tripId != null && !tripId.isBlank()) {
            redirectUrl += "&tripId=" + tripId;
        }
        response.sendRedirect(redirectUrl);
    }
}
