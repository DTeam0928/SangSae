package com.dteam0928.sangsae.quest.servlet;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;

//등록한 퀘스트의 일정 수정
//UserQuest에 일정 정보 변경
@WebServlet("/user-quests/schedule/update")
public class QuestScheduleUpdateServlet extends BaseServlet {
	
	private static final long serialVersionUID = 1L;
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		LoginUser loginUser = LoginSession.current(request);
		if (loginUser == null) {
		    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
		    return;
		}
		QuestService questService = app().questService();
		try {
			Long userQuestId = Long.parseLong(request.getParameter("userQuestId"));
			Long userId = loginUser.getId();
			LocalDate scheduledDate = LocalDate.parse(request.getParameter("scheduledDate"));
			String targetTimeParam = request.getParameter("targetTime");
			LocalTime targetTime = 
					(targetTimeParam==null || targetTimeParam.isBlank())
					? null
					: LocalTime.parse(targetTimeParam);
			String memo = request.getParameter("memo");
			UserQuest userQuest = questService.updateQuestSchedule(
					userQuestId, 
					userId, 
					scheduledDate, 
					targetTime, 
					memo);
			if(userQuest==null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND, "퀘스트를 찾을 수 없습니다.");
				return;
			}
			//TODO 프론트 통합 후 수정 완료 뒤 이동할 화면 확정
			response.sendRedirect(request.getContextPath() + "/user-quests");
		} catch (IllegalArgumentException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}
}
