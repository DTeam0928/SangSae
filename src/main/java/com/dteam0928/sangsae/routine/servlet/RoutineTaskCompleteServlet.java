package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.routine.dto.RoutineTaskCompleteResult;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.web.page.BaseServlet;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet("/user-routines/tasks/complete")
public class RoutineTaskCompleteServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        LoginUser loginUser = LoginSession.current(request);
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("로그인이 필요합니다.")));
            return;
        }

        RoutineService routineService = app().routineService();

        try {
            Long taskId = null;
            Double userLat = null;
            Double userLng = null;

            String contentType = request.getContentType();
            if (contentType != null && contentType.contains("application/json")) {
                JsonNode body = JsonUtil.mapper().readTree(request.getReader());
                if (body.has("taskId")) {
                    taskId = body.get("taskId").asLong();
                } else if (body.has("userRoutineTaskId")) {
                    taskId = body.get("userRoutineTaskId").asLong();
                }
                if (body.hasNonNull("latitude")) {
                    userLat = body.get("latitude").asDouble();
                } else if (body.hasNonNull("userLat")) {
                    userLat = body.get("userLat").asDouble();
                }
                if (body.hasNonNull("longitude")) {
                    userLng = body.get("longitude").asDouble();
                } else if (body.hasNonNull("userLng")) {
                    userLng = body.get("userLng").asDouble();
                }
            } else {
                String taskIdStr = request.getParameter("taskId");
                if (taskIdStr == null || taskIdStr.isBlank()) {
                    taskIdStr = request.getParameter("userRoutineTaskId");
                }
                if (taskIdStr != null && !taskIdStr.isBlank()) {
                    taskId = Long.parseLong(taskIdStr.trim());
                }

                String latStr = request.getParameter("latitude");
                if (latStr == null || latStr.isBlank()) {
                    latStr = request.getParameter("userLat");
                }
                if (latStr != null && !latStr.isBlank()) {
                    userLat = Double.parseDouble(latStr.trim());
                }

                String lngStr = request.getParameter("longitude");
                if (lngStr == null || lngStr.isBlank()) {
                    lngStr = request.getParameter("userLng");
                }
                if (lngStr != null && !lngStr.isBlank()) {
                    userLng = Double.parseDouble(lngStr.trim());
                }
            }

            if (taskId == null) {
                throw new IllegalArgumentException("완료할 루틴 Task ID가 필요합니다.");
            }

            RoutineTaskCompleteResult result = routineService.completeTask(
                loginUser.getId(), taskId, userLat, userLng
            );

            response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(result)));

        } catch (IllegalArgumentException | IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("루틴 Task 완료 처리 중 오류가 발생했습니다: " + e.getMessage())));
        }
    }
}
