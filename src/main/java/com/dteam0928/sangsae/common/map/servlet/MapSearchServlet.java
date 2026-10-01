package com.dteam0928.sangsae.common.map.servlet;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
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
import java.util.List;
import java.util.Map;

@WebServlet(name = "MapSearchServlet", value = "/api/map/search")
public class MapSearchServlet extends HttpServlet {

    private final PlaceSearchService searchService;
    private final Gson gson;

    public MapSearchServlet() {
        this(new PlaceSearchService());
    }

    public MapSearchServlet(PlaceSearchService searchService) {
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

        String query = request.getParameter("query");
        String searchType = request.getParameter("type");
        if (searchType == null || searchType.isBlank()) {
            searchType = "keyword";
        }

        Double latitude = parseDoubleOrNull(request.getParameter("lat"));
        Double longitude = parseDoubleOrNull(request.getParameter("lng"));
        Integer searchRadius = parseIntegerOrNull(request.getParameter("radius"));

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("api", "/api/map/search");

        if (query == null || query.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", "검색어(query) 파라미터가 누락되었습니다.");
            jsonResponse.put("count", 0);
            jsonResponse.put("data", null);
            writeJson(response, jsonResponse);
            return;
        }

        try {
            List<PlaceResponseDto> places;
            if ("address".equalsIgnoreCase(searchType.trim())) {
                places = searchService.searchAddress(query, latitude, longitude);
            } else {
                places = searchService.searchPlaces(query, latitude, longitude, searchRadius);
            }

            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("success", true);
            jsonResponse.put("code", 200);
            jsonResponse.put("message", "장소 검색 완료");
            jsonResponse.put("count", places.size());
            jsonResponse.put("data", places);

        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 500);
            jsonResponse.put("message", "서버 내부 처리 오류: " + e.getMessage());
            jsonResponse.put("count", 0);
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

    private Integer parseIntegerOrNull(String str) {
        if (str == null || str.isBlank()) return null;
        try {
            return Integer.parseInt(str.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
