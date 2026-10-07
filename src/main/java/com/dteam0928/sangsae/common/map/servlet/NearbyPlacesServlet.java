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
import java.util.List;
import java.util.Map;

@WebServlet(name = "NearbyPlacesServlet", value = "/api/places/nearby")
public class NearbyPlacesServlet extends HttpServlet {

    private final PlaceService placeService;
    private final Gson gson;

    public NearbyPlacesServlet() {
        this(new PlaceService());
    }

    public NearbyPlacesServlet(PlaceService placeService) {
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

        Double lat = parseDoubleOrNull(request.getParameter("lat"));
        Double lng = parseDoubleOrNull(request.getParameter("lng"));
        Integer radius = parseIntegerOrNull(request.getParameter("radius"));
        String category = request.getParameter("category");

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("api", "/api/places/nearby");

        if (lat == null || lng == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", "위도(lat)와 경도(lng) 파라미터는 필수입니다.");
            jsonResponse.put("count", 0);
            jsonResponse.put("data", null);
            writeJson(response, jsonResponse);
            return;
        }

        try {
            List<PlaceResponseDto> places = placeService.findNearbyPlaces(lat, lng, radius, category);

            response.setStatus(HttpServletResponse.SC_OK);
            jsonResponse.put("success", true);
            jsonResponse.put("code", 200);
            jsonResponse.put("message", "반경 내 등록 장소 목록 조회 성공");
            jsonResponse.put("count", places.size());
            jsonResponse.put("data", places);

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            jsonResponse.put("success", false);
            jsonResponse.put("code", 400);
            jsonResponse.put("message", e.getMessage());
            jsonResponse.put("count", 0);
            jsonResponse.put("data", null);
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
