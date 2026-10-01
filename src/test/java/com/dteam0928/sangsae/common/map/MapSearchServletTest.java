package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.servlet.MapSearchServlet;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

public class MapSearchServletTest {

    private PlaceSearchService mockSearchService;
    private MapSearchServlet servlet;

    @BeforeEach
    void setUp() {
        mockSearchService = mock(PlaceSearchService.class);
        servlet = new MapSearchServlet(mockSearchService);
    }

    @Test
    @DisplayName("정상 키워드 검색 시 200 OK 및 api, code, message, places 목록 반환")
    void testSearchSuccess() throws Exception {
        PlaceResponseDto place = PlaceResponseDto.builder()
                .kakaoPlaceId("18577297")
                .name("경복궁")
                .roadAddress("서울 종로구 사직로 161")
                .address("서울 종로구 세종로 1-1")
                .regions("서울특별시", "종로구", "세종로")
                .coordinates(37.579617, 126.977041)
                .theme(SangsaeTheme.CULTURE.getCode(), SangsaeTheme.CULTURE.getDisplayName())
                .originalCategoryName("여행 > 관광,명소 > 궁궐")
                .distanceInMeters(540)
                .phone("02-3700-3900")
                .placeUrl("http://place.map.kakao.com/18577297")
                .build();

        when(mockSearchService.searchPlaces(eq("경복궁"), any(), any(), any())).thenReturn(List.of(place));

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("query")).thenReturn("경복궁");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(200);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/map/search", result.get("api").getAsString());
        assertFalse(result.has("command"));
        assertTrue(result.get("success").getAsBoolean());
        assertEquals(200, result.get("code").getAsInt());
        assertEquals("장소 검색 완료", result.get("message").getAsString());
        assertEquals(1, result.get("count").getAsInt());

        JsonArray data = result.getAsJsonArray("data");
        assertEquals(1, data.size());
        assertEquals("경복궁", data.get(0).getAsJsonObject().get("name").getAsString());
    }

    @Test
    @DisplayName("검색어(query) 누락 시 400 Bad Request 및 에러 메시지 반환")
    void testQueryMissing() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        StringWriter stringWriter = new StringWriter();

        when(request.getParameter("query")).thenReturn("");
        when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));

        servlet.doGet(request, response);

        verify(response).setStatus(400);

        JsonObject result = JsonParser.parseString(stringWriter.toString()).getAsJsonObject();
        assertEquals("/api/map/search", result.get("api").getAsString());
        assertFalse(result.get("success").getAsBoolean());
        assertEquals(400, result.get("code").getAsInt());
        assertEquals("검색어(query) 파라미터가 누락되었습니다.", result.get("message").getAsString());
        assertTrue(result.get("data").isJsonNull());
    }
}
