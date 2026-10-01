package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;

@WebServlet("/quests")
public class QuestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	
	private final QuestService questService = new QuestService();
       
    public QuestServlet() {
        super();
    }

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String idParam = request.getParameter("id");
		if (idParam != null && !idParam.isBlank()) {
		    Long id = Long.parseLong(idParam);
		    QuestTemplate quest = questService.getQuestById(id);
		    if(quest==null) {
		    	response.sendError(HttpServletResponse.SC_NOT_FOUND);
		    	return;
		    }
		    List<UserQuest> userQuests = questService.getUserQuests();
		    request.setAttribute("quest", quest);
		    request.setAttribute("userQuests", userQuests);
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
		request.getRequestDispatcher("/views/quest/list.jsp").forward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
	    request.setCharacterEncoding("UTF-8");
	    Long templateId = Long.parseLong(request.getParameter("templateId"));
	    Long userId = Long.parseLong(request.getParameter("userId"));
	    Long tripId = Long.parseLong(request.getParameter("tripId"));
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
