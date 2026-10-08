
package com.dteam0928.sangsae.quest.servlet;

import java.io.IOException;
import java.util.List;

import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.util.JsonUtil;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.web.page.BaseServlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/quests/place")
public class QuestPlaceServlet extends BaseServlet {
    private static final long serialVersionUID = 1L;
    private final PlaceSearchService searchService = new PlaceSearchService();
    private final PlaceService placeService = new PlaceService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");
        if (LoginSession.current(request) == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.fail("로그인이 필요합니다.")));
            return;
        }
        try {
            String kakaoPlaceId = request.getParameter("kakaoPlaceId");
            String query = request.getParameter("query");
            if (kakaoPlaceId == null || kakaoPlaceId.isBlank()
                    || query == null || query.isBlank()) {
                throw new IllegalArgumentException("장소 검색어와 카카오 장소 ID가 필요합니다.");
            }

            // 이미 등록된 장소라면 기존 DB ID 반환
            PlaceResponseDto existing = placeService.getPlaceByKakaoPlaceId(kakaoPlaceId);

            if (existing != null) {
                response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(existing.placeId())));
                return;
            }

            // 카카오 API에서 다시 검색하여 실제 장소 정보 확인
            List<PlaceResponseDto> places = searchService.searchPlaces(query, null, null, null);
            PlaceResponseDto selected = places.stream()
                    .filter(place -> kakaoPlaceId.equals(place.kakaoPlaceId()))
                    .findFirst()
                    .orElseThrow(() ->
                        new IllegalArgumentException(
                            "선택한 장소를 검색 결과에서 확인할 수 없습니다. 다시 검색해 주세요."));

            // 신규 장소 등록 후 DB ID 반환
            Long placeId = placeService.getOrCreatePlace(selected);
            response.getWriter().write(JsonUtil.toJson(ApiResponse.ok(placeId)));

        } catch (IllegalArgumentException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write(
                JsonUtil.toJson(ApiResponse.fail(e.getMessage())));
        }
    }
}

