package com.dteam0928.sangsae;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

import com.dteam0928.sangsae.quest.QuestService;
import com.dteam0928.sangsae.quest.QuestTemplate;

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
		    request.setAttribute("quest", quest);
		    request.getRequestDispatcher("/quest/detail.jsp").forward(request, response);
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
		request.getRequestDispatcher("/quest/list.jsp").forward(request, response);
	}
}
