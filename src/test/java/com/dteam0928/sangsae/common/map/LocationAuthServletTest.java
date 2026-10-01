package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.servlet.LocationAuthServlet;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
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
import static org.mockito.Mockito.*;

public class LocationAuthServletTest {

    private PlaceService mockPlaceService;
    private LocationAuthService locationAuthService;
    private LocationAuthServlet servlet;

    private PlaceResponseDto gwangalliPlace;

    @BeforeEach
    void setUp() {
        mockPlaceService = mock(PlaceService.class);
        locationAuthService = new LocationAuthService(mockPlaceService);
        servlet = new LocationAuthServlet(locationAuthService);

        gwangalliPlace = PlaceResponseDto.builder()
                .kakaoPlaceId("18577297")
                .name("광안리 해수욕장")
                .roadAddress("부산 수영구 광안해변로 219")
                .address("부산 수영구 광안동 192-20")
                .regions("부산광역시", "수영구", "광안동")
                .coordinates(35.158697, 129.160384)
                .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                .originalCategoryName("여행 > 명소")
                .build();
    }

    @Test
    @DisplayName("성공 케이스: 50m 이내 정상 호출 시 200 OK 및 상세 데이터 반환")
    void testSuccessInvocation() throws Exception {
        when(mockPlaceService.getPlaceById(12L)).thenReturn(gwangalliPlace);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        String jsonBody = """
            {
              "placeId": 12,
              "latitude": 35.158750,
              "longitude": 129.160384,
              "radius": 50
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/map/verify-location", result.get("api").getAsString());
        assertFalse(result.has("command"));
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("SUCCESS", result.get("status").getAsString());
        assertTrue(result.get("message").getAsString().contains("위치 인증에 성공했습니다"));

        JsonObject data = result.getAsJsonObject("data");
        assertTrue(data.get("isSuccess").getAsBoolean());
        assertEquals("SUCCESS", data.get("status").getAsString());
        assertEquals(50, data.get("allowedRadiusMeters").getAsInt());
        assertTrue(data.get("distanceInMeters").getAsInt() <= 50);

        JsonObject place = data.getAsJsonObject("place");
        assertEquals("광안리 해수욕장", place.get("name").getAsString());
    }

    @Test
    @DisplayName("실패 케이스: 50m 허용 반경 초과 시 400 Bad Request 및 초과 거리 반환")
    void testDistanceExceeded() throws Exception {
        when(mockPlaceService.getPlaceById(12L)).thenReturn(gwangalliPlace);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        // 해운대 근처 좌표 (광안리에서 수 km 떨어짐)
        String jsonBody = """
            {
              "placeId": 12,
              "latitude": 35.163113,
              "longitude": 129.163554,
              "radius": 50
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/map/verify-location", result.get("api").getAsString());
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("DISTANCE_EXCEEDED", result.get("status").getAsString());
        assertTrue(result.get("message").getAsString().contains("50m 이내"));

        JsonObject data = result.getAsJsonObject("data");
        assertFalse(data.get("isSuccess").getAsBoolean());
        assertEquals("DISTANCE_EXCEEDED", data.get("status").getAsString());
        assertTrue(data.get("distanceInMeters").getAsInt() > 50);
        assertNotNull(data.get("place"));
    }

    @Test
    @DisplayName("실패 케이스: 좌표 누락 시 400 Bad Request 및 INVALID_COORDINATES 반환")
    void testMissingCoordinates() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        String jsonBody = """
            {
              "placeId": 12
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("INVALID_COORDINATES", result.get("status").getAsString());
        assertEquals("사용자 현재 GPS 좌표가 누락되었습니다.", result.get("message").getAsString());

        JsonObject data = result.getAsJsonObject("data");
        assertFalse(data.get("isSuccess").getAsBoolean());
        assertEquals("INVALID_COORDINATES", data.get("status").getAsString());
        assertEquals(-1, data.get("distanceInMeters").getAsInt());
        assertTrue(data.get("place").isJsonNull());
    }

    @Test
    @DisplayName("실패 케이스: 존재하지 않는 장소 ID일 때 400 Bad Request 및 PLACE_NOT_FOUND 반환")
    void testPlaceNotFound() throws Exception {
        when(mockPlaceService.getPlaceById(999L)).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        String jsonBody = """
            {
              "placeId": 999,
              "latitude": 35.158750,
              "longitude": 129.160384
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("PLACE_NOT_FOUND", result.get("status").getAsString());
        assertTrue(result.get("message").getAsString().contains("찾을 수 없습니다"));
    }

    @Test
    @DisplayName("서버 에러 케이스: DB 예외 발생 시 500 Internal Server Error 반환")
    void testInternalServerError() throws Exception {
        when(mockPlaceService.getPlaceById(12L)).thenThrow(new RuntimeException("DB 연결 타임아웃"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getContentType()).thenReturn("application/json; charset=UTF-8");
        String jsonBody = """
            {
              "placeId": 12,
              "latitude": 35.158750,
              "longitude": 129.160384
            }
        """;
        when(request.getReader()).thenReturn(new BufferedReader(new StringReader(jsonBody)));
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doPost(request, response);

        verify(response).setStatus(500);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(500, result.get("code").getAsInt());
        assertEquals("INTERNAL_ERROR", result.get("status").getAsString());
        assertTrue(result.get("message").getAsString().contains("DB 연결 타임아웃"));
        assertTrue(result.get("data").isJsonNull());
    }
}
