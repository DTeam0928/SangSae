package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.service.QuestService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//사용자의 퀘스트 완료 기록 조회
//QuestLog 조회
@WebServlet("/quest-logs")
public class QuestLogServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final QuestService questService = new QuestService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            // TODO auth 통합 후 로그인 세션에서 userId를 가져오도록 변경
            Long userId = Long.parseLong(request.getParameter("userId"));
            List<QuestLog> questLogs = questService.getQuestLogsByUserId(userId);
            request.setAttribute("questLogs", questLogs);
            // TODO 프론트 통합 후 완료 기록 화면 경로 확정
            request.getRequestDispatcher("/views/quest/quest-log-list.jsp").forward(request, response);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "잘못된 사용자 정보입니다.");
        }
    }
}