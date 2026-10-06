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
    	QuestService questService = app().questService();
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
        LocalDate scheduledDate = LocalDate.parse(request.getParameter("date"));
        List<UserQuest> userQuests = questService.getUserQuestsByDate(
                        userId,
                        tripId,
                        scheduledDate
                );
        request.setAttribute("userQuests", userQuests);
        //추후 통합되면 경로 바꾸기
        request.getRequestDispatcher("/views/quest/user-quest-list.jsp").forward(request, response);
    }
}
