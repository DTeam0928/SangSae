package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.common.web.Routes;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * R-00 루틴 탐색 API. 모든 GET 은 lat, lng(선택)을 받는다. 없으면 O-03 저장 위치 → 기본 위치(서울 시청).
 * <pre>
 * GET    /api/routines?q=&amp;category=&amp;placeCategory=&amp;sort=&amp;page=   검색 · 칩 · 장소 유형 · 정렬
 *                                                    (sort: distance|popular|latest|recommend)
 * GET    /api/routines/explore?placeCategory=        탐색 탭 첫 화면 전체 (장소 유형 필터 선택)
 * GET    /api/routines/{id}                          루틴 상세 모달
 * GET    /api/routines/places/{placeId}              장소 상세 모달 / 내 장소 셀렉트 변경
 * POST   /api/routines/{id}/bookmark                 찜
 * DELETE /api/routines/{id}/bookmark                 찜 해제
 * 내 루틴에 추가는 POST /user-routines/adopt (UserRoutineServlet) 를 사용한다.
 * </pre>
 * 장소 관리(/api/routines/my-places)는 {@link MyPlaceApiServlet} 이 처리한다.
 */
@WebServlet(Routes.API_ROUTINES + "/*")
public class RoutineApiServlet extends JsonApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        Double lat = doubleParam(req, "lat");
        Double lng = doubleParam(req, "lng");
        var explore = app().routineExploreService();

        if (seg.isEmpty()) {
            respond(resp, () -> explore.search(userId(req), lat, lng, param(req, "q"), param(req, "category"),
                    param(req, "placeCategory"), param(req, "sort"), intParam(req, "page", 1)));
        } else if (seg.size() == 1 && seg.get(0).equals("explore")) {
            respond(resp, () -> explore.home(userId(req), lat, lng, param(req, "placeCategory")));
        } else if (seg.size() == 1 && isId(seg.get(0))) {
            long id = Long.parseLong(seg.get(0));
            respond(resp, () -> explore.detail(userId(req), id, lat, lng));
        } else if (seg.size() == 2 && seg.get(0).equals("places") && isId(seg.get(1))) {
            long placeId = Long.parseLong(seg.get(1));
            respond(resp, () -> explore.placeDetail(userId(req), placeId, lat, lng));
        } else {
            notFound(resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        if (seg.size() != 2 || !isId(seg.get(0))) {
            notFound(resp);
            return;
        }
        long id = Long.parseLong(seg.get(0));
        if (seg.get(1).equals("bookmark")) {
            respond(resp, () -> {
                app().routineBookmarkService().bookmark(userId(req), id);
                return Map.of("bookmarked", true);
            });
        } else {
            notFound(resp);
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        if (seg.size() == 2 && isId(seg.get(0)) && seg.get(1).equals("bookmark")) {
            long id = Long.parseLong(seg.get(0));
            respond(resp, () -> {
                app().routineBookmarkService().unbookmark(userId(req), id);
                return Map.of("bookmarked", false);
            });
        } else {
            notFound(resp);
        }
    }
}
