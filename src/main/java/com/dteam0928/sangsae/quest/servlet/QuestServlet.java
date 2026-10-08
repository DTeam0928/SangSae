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
import com.dteam0928.sangsae.quest.dto.QuestDetailResponseDto;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

//공개 퀘스트를 조회, 상세조회, 일정에 등록
//QuestTemplate 조회, UserQuest에 등록
@WebServlet({"/quest", "/quests"})
public class QuestServlet extends BaseServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		QuestService questService = app().questService();
		String accept = request.getHeader("Accept");
		boolean wantsJson = accept != null && accept.contains("application/json");

		String idParam = request.getParameter("id");
		if (idParam != null && !idParam.isBlank()) {
		    Long id = Long.parseLong(idParam);
		    LoginUser loginUser = LoginSession.current(request);
		    Long userId = (loginUser != null) ? loginUser.getId() : null;
		    QuestDetailResponseDto questDetail = questService.getQuestDetail(id, userId);
		    if (questDetail == null) {
		    	response.sendError(HttpServletResponse.SC_NOT_FOUND);
		    	return;
		    }
		    if (wantsJson) {
		        response.setContentType("application/json;charset=UTF-8");
		        response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.ok(questDetail)));
		        return;
		    }
		    request.setAttribute("quest", questDetail);
		    request.setAttribute("place", questDetail.place());
		    request.getRequestDispatcher("/WEB-INF/views/quest/quest.jsp").forward(request, response);
		    return;
		}
		String category = request.getParameter("category");
		LoginUser loginUser = LoginSession.current(request);
		Long userId = (loginUser != null) ? loginUser.getId() : null;
		List<QuestDetailResponseDto> quests;
		if (category == null || category.isBlank()) {
			quests = questService.getPublicQuestDetails(userId);
		} else {
			quests = questService.getPublicQuestDetailsByCategory(category, userId);
		}
		if (wantsJson) {
		    response.setContentType("application/json;charset=UTF-8");
		    response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.ok(quests)));
		    return;
		}
		request.setAttribute("quests", quests);
		request.getRequestDispatcher("/WEB-INF/views/quest/quest.jsp").forward(request, response);
	}
	
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
	        throws ServletException, IOException {
		QuestService questService = app().questService();
	    request.setCharacterEncoding("UTF-8");
	    String accept = request.getHeader("Accept");
	    boolean wantsJson = accept != null && accept.contains("application/json");

	    String templateIdParam = request.getParameter("templateId");
	    if (templateIdParam == null || templateIdParam.isBlank()) {
	        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "templateId가 필요합니다.");
	        return;
	    }
	    Long templateId = Long.parseLong(templateIdParam.trim());
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

	    String scheduledDateParam = request.getParameter("scheduledDate");
	    LocalDate scheduledDate = (scheduledDateParam != null && !scheduledDateParam.isBlank())
	            ? LocalDate.parse(scheduledDateParam.trim())
	            : LocalDate.now();

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
	    if (wantsJson) {
	        response.setContentType("application/json;charset=UTF-8");
	        response.getWriter().write(com.dteam0928.sangsae.common.util.JsonUtil.toJson(com.dteam0928.sangsae.common.web.ApiResponse.ok(userQuest)));
	        return;
	    }
	    response.sendRedirect(request.getContextPath() + "/quest?id=" + templateId);
	}
}
