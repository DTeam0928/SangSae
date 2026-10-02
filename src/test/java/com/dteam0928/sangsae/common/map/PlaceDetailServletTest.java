package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.servlet.PlaceDetailServlet;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PlaceDetailServletTest {

    private PlaceService mockPlaceService;
    private PlaceDetailServlet servlet;

    @BeforeEach
    void setUp() {
        mockPlaceService = mock(PlaceService.class);
        servlet = new PlaceDetailServlet(mockPlaceService);
    }

    private PlaceResponseDto createSamplePlace() {
        return PlaceResponseDto.builder()
                .placeId(101L)
                .kakaoPlaceId("26338954")
                .name("광안리 해수욕장")
                .roadAddress("부산 수영구 광안해변로 219")
                .address("부산 수영구 광안동 192-20")
                .regions("부산광역시", "수영구", "광안동")
                .coordinates(35.153169, 129.118971)
                .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                .originalCategoryName("여행 > 명소 > 해수욕장,해변")
                .build();
    }

    @Test
    @DisplayName("id 파라미터로 장소 상세 조회 성공 시 200 OK 및 단건 객체 반환")
    void testPlaceDetailSuccessById() throws Exception {
        PlaceResponseDto place = createSamplePlace();
        when(mockPlaceService.getPlaceById(eq(101L))).thenReturn(place);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn("101");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/places/detail", result.get("api").getAsString());
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("장소 상세 조회 성공", result.get("message").getAsString());

        JsonObject data = result.getAsJsonObject("data");
        assertEquals(101L, data.get("placeId").getAsLong());
        assertEquals("광안리 해수욕장", data.get("name").getAsString());
        assertEquals("WALK", data.get("themeCode").getAsString());
    }

    @Test
    @DisplayName("placeId alias 파라미터로 장소 상세 조회 성공 시 200 OK 반환")
    void testPlaceDetailSuccessByPlaceIdAlias() throws Exception {
        PlaceResponseDto place = createSamplePlace();
        when(mockPlaceService.getPlaceById(eq(101L))).thenReturn(place);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("placeId")).thenReturn("101");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(101L, result.getAsJsonObject("data").get("placeId").getAsLong());
    }

    @Test
    @DisplayName("kakaoPlaceId 파라미터로 장소 상세 조회 성공 시 200 OK 반환")
    void testPlaceDetailSuccessByKakaoPlaceId() throws Exception {
        PlaceResponseDto place = createSamplePlace();
        when(mockPlaceService.getPlaceByKakaoPlaceId(eq("26338954"))).thenReturn(place);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/place/detail");
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("placeId")).thenReturn(null);
        when(request.getParameter("kakaoPlaceId")).thenReturn("26338954");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/place/detail", result.get("api").getAsString());
        assertTrue(result.get("success").getAsBoolean());
        assertEquals("26338954", result.getAsJsonObject("data").get("kakaoPlaceId").getAsString());
    }

    @Test
    @DisplayName("존재하지 않는 장소 ID 조회 시 404 Not Found 반환")
    void testPlaceDetailNotFound() throws Exception {
        when(mockPlaceService.getPlaceById(eq(9999L))).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn("9999");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(404);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(404, result.get("code").getAsInt());
        assertEquals("해당 식별자의 장소를 찾을 수 없습니다.", result.get("message").getAsString());
        assertTrue(result.get("data").isJsonNull());
    }

    @Test
    @DisplayName("식별자 파라미터가 모두 누락된 경우 400 Bad Request 반환")
    void testPlaceDetailMissingParameters() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn(null);
        when(request.getParameter("placeId")).thenReturn("");
        when(request.getParameter("kakaoPlaceId")).thenReturn(null);
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertTrue(result.get("data").isJsonNull());
    }

    @Test
    @DisplayName("유효하지 않은 숫자 형식의 ID 입력 시 400 Bad Request 반환")
    void testPlaceDetailInvalidIdFormat() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn("abc");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("장소 식별자(id 또는 placeId)는 1 이상의 양의 정수여야 합니다.", result.get("message").getAsString());
    }

    @Test
    @DisplayName("음수 또는 0인 ID 입력 시 400 Bad Request 반환")
    void testPlaceDetailNegativeId() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn("-5");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
    }

    @Test
    @DisplayName("서비스 계층 예외 발생 시 500 Internal Server Error 반환")
    void testPlaceDetailServerError() throws Exception {
        when(mockPlaceService.getPlaceById(eq(101L))).thenThrow(new RuntimeException("데이터베이스 연결 실패"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/places/detail");
        when(request.getParameter("id")).thenReturn("101");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(500);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(500, result.get("code").getAsInt());
        assertTrue(result.get("message").getAsString().contains("서버 내부 처리 오류"));
    }
}
