package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.service.QuestService;

//퀘스트를 완료 및 인증
//UserQuest 완료 및 QuestLog 생성
@WebServlet("/user-quests/complete")
public class QuestCompleteServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private final QuestService questService = new QuestService();
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) 
			throws ServletException, IOException {
		try {
			Long userQuestId = Long.parseLong(request.getParameter("userQuestId"));
			//TODO auth 통합 후 request parameter가 아니라 로그인 세션에서 userId를 가져오도록 변경
            Long userId = Long.parseLong(request.getParameter("userId"));
            String proofImageUrl = request.getParameter("proofImageUrl");
            String proofText = request.getParameter("proofText");
            QuestLog questLog = questService.completeQuest(
            	userQuestId, userId, proofImageUrl, proofText	
            );
            if(questLog==null) {
            	response.sendError(HttpServletResponse.SC_NOT_FOUND, "퀘스트를 찾을 수 없습니다.");
            	return;
            }
            response.sendRedirect(request.getContextPath() + "/user-quests");//나중에 조금 손봐야함
		} catch (IllegalArgumentException | IllegalStateException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}
}
