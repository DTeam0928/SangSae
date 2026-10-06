package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;
import java.util.List;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//사용자의 퀘스트 완료 기록 조회
//QuestLog 조회
@WebServlet("/quest-logs")
public class QuestLogServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
    	QuestService questService = app().questService();
    	try {
        	LoginUser loginUser = LoginSession.current(request);
        	if (loginUser == null) {
        	    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
        	    return;
        	}
        	Long userId = loginUser.getId();
            List<QuestLog> questLogs = questService.getQuestLogsByUserId(userId);
            request.setAttribute("questLogs", questLogs);
            // TODO 프론트 통합 후 완료 기록 화면 경로 확정
            request.getRequestDispatcher("/views/quest/quest-log-list.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 사용자 정보입니다.");
        }
    }
}