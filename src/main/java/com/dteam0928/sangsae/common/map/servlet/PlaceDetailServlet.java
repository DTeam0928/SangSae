package com.dteam0928.sangsae.common.map.servlet;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.util.ConfigUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PlaceDetailServlet", value = {"/api/places/detail", "/api/place/detail"})
public class PlaceDetailServlet extends HttpServlet {

    private final PlaceService placeService;
    private final Gson gson;

    public PlaceDetailServlet() {
        this(new PlaceService());
    }

    public PlaceDetailServlet(PlaceService placeService) {
        this.placeService = placeService;
        this.gson = new GsonBuilder().serializeNulls().disableHtmlEscaping().create();
    }

    @Override
    public void init() {
        if (getServletContext() != null) {
            String realPath = getServletContext().getRealPath("/");
            ConfigUtil.initFromRealPath(realPath);
        }
    }

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        String apiPath = request.getRequestURI();
        if (apiPath == null || apiPath.isBlank()) {
            apiPath = "/api/places/detail";
        }

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("api", apiPath);

        String idStr = request.getParameter("id");
        if (idStr == null || idStr.isBlank()) {
            idStr = request.getParameter("placeId");
        }
        String kakaoPlaceId = request.getParameter("kakaoPlaceId");

        if ((idStr == null || idStr.isBlank()) && (kakaoPlaceId == null || kakaoPlaceId.isBlank())) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", "장소 식별자(id 또는 placeId) 파라미터가 누락되었거나 유효하지 않습니다.");
            jsonResponse.put("data", null);
            writeJson(response, jsonResponse);
            return;
        }

        try {
            PlaceResponseDto place;
            if (idStr != null && !idStr.isBlank()) {
                Long placeId = parseLongOrNull(idStr);
                if (placeId == null || placeId <= 0) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    jsonResponse.put("success", false);
                    jsonResponse.put("code", 400);
                    jsonResponse.put("message", "장소 식별자(id 또는 placeId)는 1 이상의 양의 정수여야 합니다.");
                    jsonResponse.put("data", null);
                    writeJson(response, jsonResponse);
                    return;
                }
                place = placeService.getPlaceById(placeId);
            } else {
                place = placeService.getPlaceByKakaoPlaceId(kakaoPlaceId);
            }

            if (place == null) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                jsonResponse.put("success", false);
                jsonResponse.put("code", 404);
                jsonResponse.put("message", "해당 식별자의 장소를 찾을 수 없습니다.");
                jsonResponse.put("data", null);
            } else {
                response.setStatus(HttpServletResponse.SC_OK);
                jsonResponse.put("success", true);
                jsonResponse.put("code", 200);
                jsonResponse.put("message", "장소 상세 조회 성공");
                jsonResponse.put("data", place);
            }

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 500);
            jsonResponse.put("message", "서버 내부 처리 오류: " + e.getMessage());
            jsonResponse.put("data", null);
        }

        writeJson(response, jsonResponse);
    }

    private void writeJson(HttpServletResponse response, Object data) throws IOException {
        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(data));
            out.flush();
        }
    }

    private Long parseLongOrNull(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return Long.parseLong(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
