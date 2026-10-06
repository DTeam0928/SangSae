package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

//공개 퀘스트를 조회, 상세조회, 일정에 등록
//QuestTemplate 조회, UserQuest에 등록
@WebServlet("/quests")
public class QuestServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		QuestService questService = app().questService();
		String idParam = request.getParameter("id");
		if (idParam != null && !idParam.isBlank()) {
		    Long id = Long.parseLong(idParam);
		    QuestTemplate quest = questService.getQuestById(id);
		    if(quest==null) {
		    	response.sendError(HttpServletResponse.SC_NOT_FOUND);
		    	return;
		    }
		    request.setAttribute("quest", quest);
		    //경로는 추후 통합되고 나면 수정
		    request.getRequestDispatcher("/views/quest/detail.jsp").forward(request, response);
		    return;
		}
		String category = request.getParameter("category");
		List<QuestTemplate> quests;
		if(category==null||category.isBlank()) {
			quests = questService.getPublicQuests();
		}else {
			quests = questService.getPublicQuestsByCategory(category);
		}
		request.setAttribute("quests", quests);
		//경로는 추후 통합되고 나면 수정
		request.getRequestDispatcher("/views/quest/list.jsp").forward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		QuestService questService = app().questService();
	    request.setCharacterEncoding("UTF-8");
	    Long templateId = Long.parseLong(request.getParameter("templateId"));
	    LoginUser loginUser = LoginSession.current(request);
	    if (loginUser == null) {
	        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
	        return;
	    }
	    Long userId = loginUser.getId();
	    String tripIdParam = request.getParameter("tripId");
	    Long tripId = (tripIdParam == null || tripIdParam.isBlank())
	            ? null
	            : Long.parseLong(tripIdParam);
	    LocalDate scheduledDate = LocalDate.parse(request.getParameter("scheduledDate"));
	    String targetTimeParam = request.getParameter("targetTime");
	    LocalTime targetTime = null;
	    if (targetTimeParam != null && !targetTimeParam.isBlank())
	        targetTime = LocalTime.parse(targetTimeParam);
	    String authRadiusMParam = request.getParameter("authRadiusM");
	    Integer authRadiusM = null;
	    if (authRadiusMParam != null && !authRadiusMParam.isBlank())
	        authRadiusM = Integer.parseInt(authRadiusMParam);
	    UserQuest userQuest = questService.addQuestToSchedule(
	            templateId,
	            userId,
	            tripId,
	            scheduledDate,
	            targetTime,
	            authRadiusM
	    );
	    if (userQuest == null) {
	        response.sendError(HttpServletResponse.SC_NOT_FOUND);
	        return;
	    }
	    response.sendRedirect(request.getContextPath() + "/quests?id=" + templateId);
	}
}
