package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.servlet.NearbyPlacesServlet;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class NearbyPlacesServletTest {

    private PlaceService mockPlaceService;
    private NearbyPlacesServlet servlet;

    @BeforeEach
    void setUp() {
        mockPlaceService = mock(PlaceService.class);
        servlet = new NearbyPlacesServlet(mockPlaceService);
    }

    @Test
    @DisplayName("반경 내 장소 목록 조회 성공 시 200 OK 및 장소 목록 반환")
    void testNearbyPlacesSuccess() throws Exception {
        PlaceResponseDto place = PlaceResponseDto.builder()
                .placeId(101L)
                .kakaoPlaceId("26338954")
                .name("광안리 해수욕장")
                .roadAddress("부산 수영구 광안해변로 219")
                .address("부산 수영구 광안동 192-20")
                .regions("부산광역시", "수영구", "광안동")
                .coordinates(35.153169, 129.118971)
                .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                .originalCategoryName("여행 > 명소 > 해수욕장,해변")
                .distanceInMeters(150)
                .phone("051-610-4216")
                .placeUrl("http://place.map.kakao.com/26338954")
                .build();

        when(mockPlaceService.findNearbyPlaces(eq(35.153169), eq(129.118971), eq(3000), eq("WALK")))
                .thenReturn(List.of(place));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(request.getParameter("radius")).thenReturn("3000");
        when(request.getParameter("category")).thenReturn("WALK");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/places/nearby", result.get("api").getAsString());
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("반경 내 등록 장소 목록 조회 성공", result.get("message").getAsString());
        assertEquals(1, result.get("count").getAsInt());

        JsonArray data = result.getAsJsonArray("data");
        assertEquals(1, data.size());
        JsonObject first = data.get(0).getAsJsonObject();
        assertEquals(101L, first.get("placeId").getAsLong());
        assertEquals("광안리 해수욕장", first.get("name").getAsString());
        assertEquals(150, first.get("distanceInMeters").getAsInt());
    }

    @Test
    @DisplayName("조회 결과가 0건일 때 200 OK 및 빈 배열 반환")
    void testNearbyPlacesEmptyList() throws Exception {
        when(mockPlaceService.findNearbyPlaces(eq(35.153169), eq(129.118971), isNull(), isNull()))
                .thenReturn(Collections.emptyList());

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals(0, result.get("count").getAsInt());
        assertEquals(0, result.getAsJsonArray("data").size());
    }

    @Test
    @DisplayName("위도 또는 경도 누락 시 400 Bad Request 반환")
    void testMissingCoordinates() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/places/nearby", result.get("api").getAsString());
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertTrue(result.get("data").isJsonNull());
    }

    @Test
    @DisplayName("유효하지 않은 좌표 입력 시 400 Bad Request 반환")
    void testInvalidCoordinates() throws Exception {
        when(mockPlaceService.findNearbyPlaces(eq(999.0), eq(129.0), any(), any()))
                .thenThrow(new IllegalArgumentException("유효하지 않은 위도/경도 좌표 범위입니다."));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("lat")).thenReturn("999.0");
        when(request.getParameter("lng")).thenReturn("129.0");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("유효하지 않은 위도/경도 좌표 범위입니다.", result.get("message").getAsString());
    }

    @Test
    @DisplayName("서비스 계층 예외 발생 시 500 Internal Server Error 반환")
    void testServerError() throws Exception {
        when(mockPlaceService.findNearbyPlaces(anyDouble(), anyDouble(), any(), any()))
                .thenThrow(new RuntimeException("DB 접속 오류"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(500);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(500, result.get("code").getAsInt());
        assertTrue(result.get("message").getAsString().contains("서버 내부 처리 오류"));
    }
}
