package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.routine.dto.RoutineTemplateDetailDto;
import com.dteam0928.sangsae.routine.entity.RoutineTemplate;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/routines")
public class RoutineCatalogServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("application/json;charset=UTF-8");
        RoutineService routineService = app().routineService();
        String idParam = request.getParameter("id");

        if (idParam != null && !idParam.isBlank()) {
            try {
                Long templateId = Long.parseLong(idParam.trim());
                RoutineTemplateDetailDto detail = routineService.getTemplateDetail(templateId);
                if (detail == null) {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("존재하지 않는 루틴 템플릿입니다.")));
                    return;
                }
                response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(detail)));
                return;
            } catch (NumberFormatException e) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("올바른 루틴 ID를 입력해주세요.")));
                return;
            }
        }

        String category = request.getParameter("category");
        List<RoutineTemplate> templates = routineService.getPublicTemplates(category);
        response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(templates)));
    }
}
