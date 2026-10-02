package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.AddressResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.servlet.Coord2AddressServlet;
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

public class Coord2AddressServletTest {

    private PlaceSearchService mockSearchService;
    private Coord2AddressServlet servlet;

    @BeforeEach
    void setUp() {
        mockSearchService = mock(PlaceSearchService.class);
        servlet = new Coord2AddressServlet(mockSearchService);
    }

    private AddressResponseDto createSampleAddress() {
        return AddressResponseDto.builder()
                .roadAddress("부산광역시 수영구 광안해변로 219")
                .address("부산광역시 수영구 광안동 192-20")
                .regions("부산광역시", "수영구", "광안동")
                .zoneNo("48283")
                .buildingName("광안리 해수욕장 종합안내소")
                .coordinates(35.153169, 129.118971)
                .build();
    }

    @Test
    @DisplayName("유효한 좌표 전달 시 200 OK 및 주소 역변환 데이터 반환")
    void testCoord2AddressSuccess() throws Exception {
        AddressResponseDto sample = createSampleAddress();
        when(mockSearchService.coordToAddress(eq(35.153169), eq(129.118971))).thenReturn(sample);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/map/coord2address", result.get("api").getAsString());
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("좌표 기반 주소 변환 성공", result.get("message").getAsString());

        JsonObject data = result.getAsJsonObject("data");
        assertEquals("부산광역시 수영구 광안해변로 219", data.get("roadAddress").getAsString());
        assertEquals("부산광역시 수영구 광안동 192-20", data.get("address").getAsString());
        assertEquals("부산광역시", data.get("region1Depth").getAsString());
        assertEquals("수영구", data.get("region2Depth").getAsString());
        assertEquals("광안동", data.get("region3Depth").getAsString());
        assertEquals("48283", data.get("zoneNo").getAsString());
    }

    @Test
    @DisplayName("해당 좌표에 주소 정보가 없는 경우 200 OK 및 data: null 반환")
    void testCoord2AddressNotFound() throws Exception {
        when(mockSearchService.coordToAddress(eq(35.0), eq(130.0))).thenReturn(null);

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
        when(request.getParameter("lat")).thenReturn("35.0");
        when(request.getParameter("lng")).thenReturn("130.0");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("해당 좌표에 일치하는 주소 정보가 없습니다.", result.get("message").getAsString());
        assertTrue(result.get("data").isJsonNull());
    }

    @Test
    @DisplayName("위도(lat) 파라미터 누락 시 400 Bad Request 반환")
    void testMissingLat() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
        when(request.getParameter("lat")).thenReturn(null);
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("위도(lat)와 경도(lng) 파라미터는 필수입니다.", result.get("message").getAsString());
    }

    @Test
    @DisplayName("경도(lng) 파라미터 누락 시 400 Bad Request 반환")
    void testMissingLng() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
        when(request.getParameter("lat")).thenReturn("35.153169");
        when(request.getParameter("lng")).thenReturn("");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
    }

    @Test
    @DisplayName("숫자가 아닌 위경도 문자열 입력 시 400 Bad Request 반환")
    void testInvalidNumberFormat() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
        when(request.getParameter("lat")).thenReturn("not_a_number");
        when(request.getParameter("lng")).thenReturn("129.118971");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
    }

    @Test
    @DisplayName("위경도 유효 범위 초과 시 400 Bad Request 반환")
    void testCoordinatesOutOfBounds() throws Exception {
        when(mockSearchService.coordToAddress(eq(999.0), eq(129.0)))
                .thenThrow(new IllegalArgumentException("유효하지 않은 위도/경도 좌표 범위입니다."));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
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
    @DisplayName("카카오 API 호출 등 서버 내부 오류 발생 시 500 Internal Server Error 반환")
    void testServerError() throws Exception {
        when(mockSearchService.coordToAddress(anyDouble(), anyDouble()))
                .thenThrow(new RuntimeException("카카오 로컬 API 통신 실패"));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getRequestURI()).thenReturn("/api/map/coord2address");
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
