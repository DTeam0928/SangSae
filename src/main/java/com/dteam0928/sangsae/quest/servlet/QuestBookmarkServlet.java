package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuestBookmark;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

//퀘스트 북마크 관리
//UserQuestBookmark를 추가, 삭제, 조회
@WebServlet("/quest-bookmarks")
public class QuestBookmarkServlet extends BaseServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        LoginUser loginUser = LoginSession.current(request);
        if (loginUser == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
            return;
        }

        List<QuestTemplate> bookmarks =
                app().questService().getBookmarkedQuests(loginUser.getId());

        response.setContentType("application/json;charset=UTF-8");

        StringBuilder json = new StringBuilder("{\"success\":true,\"data\":[");
        for (int i = 0; i < bookmarks.size(); i++) {
            if (i > 0) json.append(',');
            json.append("{\"id\":")
                    .append(bookmarks.get(i).getId())
                    .append('}');
        }
        json.append("]}");

        response.getWriter().write(json.toString());
    }
    
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
    	QuestService questService = app().questService();
    	LoginUser loginUser = LoginSession.current(request);
    	if (loginUser == null) {
    	    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
    	    return;
    	}
    	Long userId = loginUser.getId();
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
    	QuestService questService = app().questService();
    	LoginUser loginUser = LoginSession.current(request);
    	if (loginUser == null) {
    	    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "로그인이 필요합니다.");
    	    return;
    	}
    	Long userId = loginUser.getId();
        Long questTemplateId = Long.parseLong(request.getParameter("questTemplateId"));
        boolean removed = questService.removeBookmark(userId, questTemplateId);
        if (!removed) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        response.setStatus(HttpServletResponse.SC_NO_CONTENT);
    }
}
