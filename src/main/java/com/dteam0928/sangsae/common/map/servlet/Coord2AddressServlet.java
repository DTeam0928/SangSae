package com.dteam0928.sangsae.common.map.servlet;

import com.dteam0928.sangsae.common.map.dto.AddressResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
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

@WebServlet(name = "Coord2AddressServlet", value = {"/api/map/coord2address", "/api/map/reverse-geocode"})
public class Coord2AddressServlet extends HttpServlet {

    private final PlaceSearchService searchService;
    private final Gson gson;

    public Coord2AddressServlet() {
        this(new PlaceSearchService());
    }

    public Coord2AddressServlet(PlaceSearchService searchService) {
        this.searchService = searchService;
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
            apiPath = "/api/map/coord2address";
        }

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("api", apiPath);

        Double lat = parseDoubleOrNull(request.getParameter("lat"));
        Double lng = parseDoubleOrNull(request.getParameter("lng"));

        if (lat == null || lng == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", "위도(lat)와 경도(lng) 파라미터는 필수입니다.");
            jsonResponse.put("data", null);
            writeJson(response, jsonResponse);
            return;
        }

        try {
            AddressResponseDto addressDto = searchService.coordToAddress(lat, lng);

            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("success", true);
            jsonResponse.put("code", 200);

            if (addressDto != null) {
                jsonResponse.put("message", "좌표 기반 주소 변환 성공");
                jsonResponse.put("data", addressDto);
            } else {
                jsonResponse.put("message", "해당 좌표에 일치하는 주소 정보가 없습니다.");
                jsonResponse.put("data", null);
            }

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

    private Double parseDoubleOrNull(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return Double.parseDouble(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
