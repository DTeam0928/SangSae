package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;

@WebServlet("/user-quests")
public class UserQuestServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private final QuestService questService = new QuestService();

    public UserQuestServlet() {
        super();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Long userId = Long.parseLong(request.getParameter("userId"));
        Long tripId = Long.parseLong(request.getParameter("tripId"));
        LocalDate scheduledDate = LocalDate.parse(request.getParameter("date"));
        List<UserQuest> userQuests = questService.getUserQuestsByDate(
                        userId,
                        tripId,
                        scheduledDate
                );
        request.setAttribute("userQuests", userQuests);
        request.getRequestDispatcher("/views/quest/user-quest-list.jsp").forward(request, response);
    }
}
