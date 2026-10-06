package com.dteam0928.sangsae.quest.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.quest.dto.QuestCompleteResult;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.web.page.BaseServlet;

import java.util.LinkedHashMap;
import java.util.Map;

//퀘스트를 완료 및 인증
//UserQuest 완료, QuestLog 생성 및 경험치 보상 지급
@WebServlet("/user-quests/complete")
public class QuestCompleteServlet extends BaseServlet {
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
		String accept = request.getHeader("Accept");
		boolean wantsJson = accept != null && accept.contains("application/json");

		try {
			Long userQuestId = Long.parseLong(request.getParameter("userQuestId"));
			Long userId = loginUser.getId();
            String proofImageUrl = request.getParameter("proofImageUrl");
            String proofText = request.getParameter("proofText");

            String latParam = request.getParameter("latitude");
            if (latParam == null || latParam.isBlank()) {
                latParam = request.getParameter("userLat");
            }
            String lngParam = request.getParameter("longitude");
            if (lngParam == null || lngParam.isBlank()) {
                lngParam = request.getParameter("userLng");
            }

            Double userLat = (latParam != null && !latParam.isBlank()) ? Double.parseDouble(latParam.trim()) : null;
            Double userLng = (lngParam != null && !lngParam.isBlank()) ? Double.parseDouble(lngParam.trim()) : null;

            QuestCompleteResult result = questService.completeQuestWithReward(
                userQuestId, userId, proofImageUrl, proofText, userLat, userLng
            );
            if (result == null) {
                if (wantsJson) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("퀘스트를 찾을 수 없습니다.")));
                    return;
                }
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "퀘스트를 찾을 수 없습니다.");
                return;
            }

            if (wantsJson) {
                response.setStatus(HttpServletResponse.SC_OK);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> respData = new LinkedHashMap<>();
                respData.put("questLogId", result.questLog().getId());
                respData.put("earnedXp", result.questLog().getEarnedXp());
                respData.put("reward", result.expRewardResult());
                response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(respData)));
                return;
            }

            response.sendRedirect(request.getContextPath() + "/user-quests");
		} catch (IllegalArgumentException | IllegalStateException e) {
			if (wantsJson) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.setContentType("application/json;charset=UTF-8");
				response.getWriter().write(JsonUtil.toJson(ApiResponse.fail(e.getMessage())));
				return;
			}
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
		}
	}
}
