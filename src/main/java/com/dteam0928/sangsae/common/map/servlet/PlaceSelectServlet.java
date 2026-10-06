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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "PlaceSelectServlet", value = "/api/place/select")
public class PlaceSelectServlet extends HttpServlet {

    private final PlaceService placeService;
    private final Gson gson;

    public PlaceSelectServlet() {
        this(new PlaceService());
    }

    public PlaceSelectServlet(PlaceService placeService) {
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
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("api", "/api/place/select");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        String requestBody = sb.toString();
        if (requestBody.isBlank()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", "요청 본문(JSON)이 비어있습니다.");
            jsonResponse.put("data", null);
            writeJson(response, jsonResponse);
            return;
        }

        try {
            com.google.gson.JsonObject rootJson = com.google.gson.JsonParser.parseString(requestBody).getAsJsonObject();
            com.google.gson.JsonObject targetJson = (rootJson.has("data") && rootJson.get("data").isJsonObject())
                    ? rootJson.getAsJsonObject("data")
                    : rootJson;

            PlaceResponseDto dto = gson.fromJson(targetJson, PlaceResponseDto.class);
            Long placeId = placeService.getOrCreatePlace(dto);

            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("placeId", placeId);
            dataMap.put("kakaoPlaceId", dto.kakaoPlaceId());
            dataMap.put("name", dto.name());
            dataMap.put("roadAddress", dto.roadAddress());
            dataMap.put("latitude", dto.latitude());
            dataMap.put("longitude", dto.longitude());
            dataMap.put("themeCode", dto.themeCode());
            dataMap.put("themeName", dto.themeName());

            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("success", true);
            jsonResponse.put("code", 200);
            jsonResponse.put("message", "장소가 성공적으로 등록 및 선택되었습니다.");
            jsonResponse.put("data", dataMap);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", e.getMessage());
            jsonResponse.put("data", null);
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
}
