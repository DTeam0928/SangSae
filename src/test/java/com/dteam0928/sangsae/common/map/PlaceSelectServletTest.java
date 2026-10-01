package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.servlet.PlaceSelectServlet;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PlaceSelectServletTest {

    private PlaceService mockPlaceService;
    private PlaceSelectServlet servlet;

    @BeforeEach
    void setUp() {
        mockPlaceService = mock(PlaceService.class);
        servlet = new PlaceSelectServlet(mockPlaceService);
    }

    @Test
    @DisplayName("평평한(Flat) JSON 요청으로 장소 선택 시 200 OK 및 api, placeId 반환")
    void testPlaceSelectFlatJsonSuccess() throws Exception {
        when(mockPlaceService.getOrCreatePlace(any(PlaceResponseDto.class))).thenReturn(1L);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        String jsonBody = """
            {
              "kakaoPlaceId": "18577297",
              "name": "경복궁",
              "roadAddress": "서울 종로구 사직로 161",
              "latitude": 37.579617,
              "longitude": 126.977041,
              "themeCode": "CULTURE",
              "themeName": "#문화/예술"
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/place/select", result.get("api").getAsString());
        assertFalse(result.has("command"));
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("장소가 성공적으로 등록 및 선택되었습니다.", result.get("message").getAsString());

        JsonObject data = result.getAsJsonObject("data");
        assertEquals(1L, data.get("placeId").getAsLong());
        assertEquals("경복궁", data.get("name").getAsString());
        assertEquals(37.579617, data.get("latitude").getAsDouble(), 0.0001);
    }

    @Test
    @DisplayName("빈 본문 전달 시 400 Bad Request 및 에러 메시지 반환")
    void testEmptyBody() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getReader()).thenReturn(new BufferedReader(new StringReader("   ")));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/place/select", result.get("api").getAsString());
        assertFalse(result.has("command"));
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertTrue(result.get("data").isJsonNull());
    }
}
