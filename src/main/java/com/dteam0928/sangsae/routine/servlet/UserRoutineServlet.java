package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskCreateRequest;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskResponseDto;
import com.dteam0928.sangsae.routine.entity.UserRoutine;
import com.dteam0928.sangsae.routine.entity.UserRoutineTask;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.web.page.BaseServlet;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/user-routines", "/user-routines/adopt", "/user-routines/tasks"})
public class UserRoutineServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        LoginUser loginUser = LoginSession.current(request);
        if (loginUser == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("로그인이 필요합니다.")));
            return;
        }

        RoutineService routineService = app().routineService();
        String dateParam = request.getParameter("date");
        LocalDate targetDate = LocalDate.now();
        if (dateParam != null && !dateParam.isBlank()) {
            try {
                targetDate = LocalDate.parse(dateParam.trim());
            } catch (Exception ignored) {
            }
        }

        List<UserRoutineTaskResponseDto> timeline = routineService.getTodayTimeline(loginUser.getId(), targetDate);
        response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(timeline)));
    }

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

        String path = request.getServletPath();
        RoutineService routineService = app().routineService();

        try {
            if ("/user-routines/adopt".equals(path)) {
                handleAdoptRoutine(request, response, loginUser.getId(), routineService);
            } else if ("/user-routines/tasks".equals(path)) {
                handleAddTask(request, response, loginUser.getId(), routineService);
            } else {
                handleCreateRoutine(request, response, loginUser.getId(), routineService);
            }
        } catch (IllegalArgumentException | IllegalStateException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail(e.getMessage())));
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("루틴 처리 중 오류가 발생했습니다: " + e.getMessage())));
        }
    }

    // 템플릿을 내 루틴으로 담기 (Fork)
    private void handleAdoptRoutine(HttpServletRequest request, HttpServletResponse response,
                                    Long userId, RoutineService routineService) throws IOException {
        Long templateId = null;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = null;

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            JsonNode body = JsonUtil.mapper().readTree(request.getReader());
            if (body.has("routineTemplateId")) {
                templateId = body.get("routineTemplateId").asLong();
            } else if (body.has("templateId")) {
                templateId = body.get("templateId").asLong();
            }
            if (body.hasNonNull("startDate")) {
                startDate = LocalDate.parse(body.get("startDate").asText());
            }
            if (body.hasNonNull("endDate")) {
                endDate = LocalDate.parse(body.get("endDate").asText());
            }
        } else {
            String idStr = request.getParameter("routineTemplateId");
            if (idStr == null || idStr.isBlank()) {
                idStr = request.getParameter("templateId");
            }
            if (idStr != null && !idStr.isBlank()) {
                templateId = Long.parseLong(idStr.trim());
            }
            String startStr = request.getParameter("startDate");
            if (startStr != null && !startStr.isBlank()) {
                startDate = LocalDate.parse(startStr.trim());
            }
            String endStr = request.getParameter("endDate");
            if (endStr != null && !endStr.isBlank()) {
                endDate = LocalDate.parse(endStr.trim());
            }
        }

        if (templateId == null) {
            throw new IllegalArgumentException("루틴 템플릿 ID가 필요합니다.");
        }

        UserRoutine userRoutine = routineService.adoptRoutine(userId, templateId, startDate, endDate);
        response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(userRoutine)));
    }

    // 나만의 루틴 직접 생성
    private void handleCreateRoutine(HttpServletRequest request, HttpServletResponse response,
                                     Long userId, RoutineService routineService) throws IOException {
        String title = null;
        LocalDate startDate = LocalDate.now();
        LocalDate endDate = null;

        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            JsonNode body = JsonUtil.mapper().readTree(request.getReader());
            if (body.hasNonNull("title")) {
                title = body.get("title").asText();
            }
            if (body.hasNonNull("startDate")) {
                startDate = LocalDate.parse(body.get("startDate").asText());
            }
            if (body.hasNonNull("endDate")) {
                endDate = LocalDate.parse(body.get("endDate").asText());
            }
        } else {
            title = request.getParameter("title");
            String startStr = request.getParameter("startDate");
            if (startStr != null && !startStr.isBlank()) {
                startDate = LocalDate.parse(startStr.trim());
            }
            String endStr = request.getParameter("endDate");
            if (endStr != null && !endStr.isBlank()) {
                endDate = LocalDate.parse(endStr.trim());
            }
        }

        UserRoutine created = routineService.createCustomRoutine(userId, title, startDate, endDate);
        response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(created)));
    }

    // 세부 Task 직접 추가 (가변 반경 50m~1km 지원)
    private void handleAddTask(HttpServletRequest request, HttpServletResponse response,
                               Long userId, RoutineService routineService) throws IOException {
        UserRoutineTaskCreateRequest taskReq = null;
        String contentType = request.getContentType();

        if (contentType != null && contentType.contains("application/json")) {
            taskReq = JsonUtil.mapper().readValue(request.getReader(), UserRoutineTaskCreateRequest.class);
        } else {
            Long userRoutineId = Long.parseLong(request.getParameter("userRoutineId"));
            String content = request.getParameter("content");
            String targetTime = request.getParameter("targetTime");
            String category = request.getParameter("category");
            String repeatType = request.getParameter("repeatType");
            String placeIdStr = request.getParameter("placeId");
            Long placeId = (placeIdStr != null && !placeIdStr.isBlank()) ? Long.parseLong(placeIdStr.trim()) : null;

            String radiusStr = request.getParameter("allowedRadiusMeters");
            Integer allowedRadius = (radiusStr != null && !radiusStr.isBlank()) ? Integer.parseInt(radiusStr.trim()) : 50;

            String alertTime = request.getParameter("alertTime");
            String memo = request.getParameter("memo");

            String[] days = request.getParameterValues("daysOfWeek");
            List<String> daysOfWeek = days != null ? List.of(days) : new ArrayList<>();

            taskReq = new UserRoutineTaskCreateRequest(
                userRoutineId, content, targetTime, category, repeatType,
                placeId, allowedRadius, alertTime, memo, daysOfWeek
            );
        }

        UserRoutineTask createdTask = routineService.addCustomTask(userId, taskReq);
        response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(createdTask)));
    }
}
