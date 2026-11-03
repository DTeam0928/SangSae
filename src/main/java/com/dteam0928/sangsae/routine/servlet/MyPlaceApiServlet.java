package com.dteam0928.sangsae.routine.servlet;

import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.routine.dto.PlaceRegisterRequest;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.List;

/**
 * 내가 다니는 장소 (장소 관리). 응답 data 는 항상 최신 내 장소 목록.
 * <pre>
 * GET    /api/routines/my-places?lat=&amp;lng=     목록 (좌표를 주면 거리 포함)
 * POST   /api/routines/my-places                 등록 (form)
 *          기존 장소: placeId
 *          카카오 검색 결과: kakaoPlaceId, name, latitude(카카오 y), longitude(카카오 x),
 *                           roadAddress, addressName(또는 region1~3), category
 * DELETE /api/routines/my-places/{placeId}       삭제
 * </pre>
 */
@WebServlet(Routes.API_MY_PLACES + "/*")
public class MyPlaceApiServlet extends JsonApiServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!segments(req).isEmpty()) {
            notFound(resp);
            return;
        }
        respond(resp, () -> app().myPlaceService().list(userId(req), doubleParam(req, "lat"), doubleParam(req, "lng")));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!segments(req).isEmpty()) {
            notFound(resp);
            return;
        }
                PlaceRegisterRequest form = placeForm(req);
        respond(resp, () -> app().myPlaceService().register(userId(req), form));
    }

    /** 카카오 장소 form 값 (Task 장소 저장 POST /api/routines/places 에서도 사용) */
    static PlaceRegisterRequest placeForm(HttpServletRequest req) {
        PlaceRegisterRequest form = new PlaceRegisterRequest();
        form.setPlaceId(longParam(req, "placeId"));
        form.setKakaoPlaceId(param(req, "kakaoPlaceId"));
        form.setName(param(req, "name"));
        form.setRoadAddress(param(req, "roadAddress"));
        form.setAddressName(param(req, "addressName"));
        form.setRegion1(param(req, "region1"));
        form.setRegion2(param(req, "region2"));
        form.setRegion3(param(req, "region3"));
        form.setLatitude(doubleParam(req, "latitude"));
        form.setLongitude(doubleParam(req, "longitude"));
        form.setCategory(param(req, "category"));
        return form;
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        List<String> seg = segments(req);
        if (seg.size() != 1 || !isId(seg.get(0))) {
            notFound(resp);
            return;
        }
        long placeId = Long.parseLong(seg.get(0));
        respond(resp, () -> app().myPlaceService().remove(userId(req), placeId));
    }
}
