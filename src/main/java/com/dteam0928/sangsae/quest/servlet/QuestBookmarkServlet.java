package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;

import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;
import com.dteam0928.sangsae.quest.service.QuestService;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/quest-bookmarks")
public class QuestBookmarkServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private final QuestService questService = new QuestService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("add".equals(action)) {
            addBookmark(request, response);
            return;
        }
        if ("remove".equals(action)) {
            removeBookmark(request, response);
            return;
        }
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
    }
    //북마크 추가
    private void addBookmark(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long userId = Long.parseLong(request.getParameter("userId"));
        Long questTemplateId = Long.parseLong(request.getParameter("questTemplateId"));
        String folderName = request.getParameter("folderName");
        UserQuestBookmark bookmark = questService.addBookmark(
                        userId,
                        questTemplateId,
                        folderName);
        if (bookmark == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
    //북마크 제거
    private void removeBookmark(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        Long userId = Long.parseLong(request.getParameter("userId"));
        Long questTemplateId = Long.parseLong(request.getParameter("questTemplateId"));
        boolean removed = questService.removeBookmark(userId, questTemplateId);
        if (!removed) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
