package com.dteam0928.sangsae.common.map.servlet;

import com.dteam0928.sangsae.common.map.dto.LocationAuthResult;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.util.ConfigUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

@WebServlet(name = "LocationAuthServlet", value = "/api/map/verify-location")
public class LocationAuthServlet extends HttpServlet {

    private final LocationAuthService locationAuthService;
    private final Gson gson;

    public LocationAuthServlet() {
        this(new LocationAuthService());
    }

    public LocationAuthServlet(LocationAuthService locationAuthService) {
        this.locationAuthService = locationAuthService;
        this.gson = new GsonBuilder().disableHtmlEscaping().create();
    }

    @Override
    public void init() {
        if (getServletContext() != null) {
            String realPath = getServletContext().getRealPath("/");
            ConfigUtil.initFromRealPath(realPath);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processVerification(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processVerification(request, response);
    }

    private void processVerification(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> jsonResponse = new HashMap<>();
        jsonResponse.put("command", "VERIFY_LOCATION");

        Long placeId = null;
        Double userLat = null;
        Double userLng = null;
        int radius = LocationAuthService.DEFAULT_ALLOWED_RADIUS_METERS;

        // JSON Body 또는 쿼리 파라미터 유연 지원
        String contentType = request.getContentType();
        if (contentType != null && contentType.contains("application/json")) {
            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }
            String body = sb.toString();
            if (!body.isBlank()) {
                try {
                    JsonObject json = JsonParser.parseString(body).getAsJsonObject();
                    if (json.has("placeId") && !json.get("placeId").isJsonNull()) {
                        placeId = json.get("placeId").getAsLong();
                    }
                    if (json.has("latitude") && !json.get("latitude").isJsonNull()) {
                        userLat = json.get("latitude").getAsDouble();
                    } else if (json.has("lat") && !json.get("lat").isJsonNull()) {
                        userLat = json.get("lat").getAsDouble();
                    }
                    if (json.has("longitude") && !json.get("longitude").isJsonNull()) {
                        userLng = json.get("longitude").getAsDouble();
                    } else if (json.has("lng") && !json.get("lng").isJsonNull()) {
                        userLng = json.get("lng").getAsDouble();
                    }
                    if (json.has("radius") && !json.get("radius").isJsonNull()) {
                        radius = json.get("radius").getAsInt();
                    }
                } catch (Exception ignored) {}
            }
        }

        if (placeId == null && request.getParameter("placeId") != null) {
            try { placeId = Long.parseLong(request.getParameter("placeId")); } catch (NumberFormatException ignored) {}
        }
        if (userLat == null) {
            String latParam = request.getParameter("latitude") != null ? request.getParameter("latitude") : request.getParameter("lat");
            if (latParam != null) {
                try { userLat = Double.parseDouble(latParam); } catch (NumberFormatException ignored) {}
            }
        }
        if (userLng == null) {
            String lngParam = request.getParameter("longitude") != null ? request.getParameter("longitude") : request.getParameter("lng");
            if (lngParam != null) {
                try { userLng = Double.parseDouble(lngParam); } catch (NumberFormatException ignored) {}
            }
        }
        if (request.getParameter("radius") != null) {
            try { radius = Integer.parseInt(request.getParameter("radius")); } catch (NumberFormatException ignored) {}
        }

        LocationAuthResult result = locationAuthService.verifyLocation(placeId, userLat, userLng, radius);

        response.setStatus(result.isSuccess() ? HttpServletResponse.SC_OK : HttpServletResponse.SC_BAD_REQUEST);
        jsonResponse.put("success", result.isSuccess());
        jsonResponse.put("code", result.isSuccess() ? 200 : 400);
        jsonResponse.put("status", result.status().name());
        jsonResponse.put("message", result.message());
        jsonResponse.put("data", result);

        try (PrintWriter out = response.getWriter()) {
            out.print(gson.toJson(jsonResponse));
            out.flush();
        }
    }
}
