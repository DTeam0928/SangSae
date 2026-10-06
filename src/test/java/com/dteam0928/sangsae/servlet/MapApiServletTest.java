package com.dteam0928.sangsae.servlet;

import com.dteam0928.sangsae.common.map.servlet.*;
import com.dteam0928.sangsae.testutil.ServletTestHelper;
import com.dteam0928.sangsae.testutil.ServletTestHelper.MockContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapApiServletTest {

    @Test
    @DisplayName("GET /api/map/search: 검색어(query) 누락 시 400 Bad Request 응답")
    void testMapSearchServlet_missingQuery() throws Exception {
        MapSearchServlet servlet = new MapSearchServlet();
        MockContext ctx = new MockContext();
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doGet(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("query"));
    }

    @Test
    @DisplayName("GET /api/map/coord2address: 위경도 누락 시 400 Bad Request 응답")
    void testCoord2AddressServlet_missingCoords() throws Exception {
        Coord2AddressServlet servlet = new Coord2AddressServlet();
        MockContext ctx = new MockContext();
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doGet(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("위도"));
    }

    @Test
    @DisplayName("GET /api/places/nearby: 위경도 누락 시 400 Bad Request 응답")
    void testNearbyPlacesServlet_missingCoords() throws Exception {
        NearbyPlacesServlet servlet = new NearbyPlacesServlet();
        MockContext ctx = new MockContext();
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doGet(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("위도"));
    }

    @Test
    @DisplayName("GET /api/places/detail: 식별자 파라미터 누락 시 400 Bad Request 응답")
    void testPlaceDetailServlet_missingId() throws Exception {
        PlaceDetailServlet servlet = new PlaceDetailServlet();
        MockContext ctx = new MockContext();
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doGet(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("식별자"));
    }

    @Test
    @DisplayName("POST /api/place/select: 본문 누락 시 400 Bad Request 응답")
    void testPlaceSelectServlet_emptyBody() throws Exception {
        PlaceSelectServlet servlet = new PlaceSelectServlet();
        MockContext ctx = new MockContext();
        ctx.requestBody = "";
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doPost(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("요청"));
    }

    @Test
    @DisplayName("POST /api/map/verify-location: 장소 식별자(placeId) 누락 시 400 응답")
    void testLocationAuthServlet_verify() throws Exception {
        LocationAuthServlet servlet = new LocationAuthServlet();
        MockContext ctx = new MockContext();
        ctx.requestBody = "{\"userLat\": 37.5760, \"userLng\": 126.9769}";
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.doPost(req, res);

        assertEquals(400, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("장소") || ctx.responseBody.toString().contains("false"));
    }
}
