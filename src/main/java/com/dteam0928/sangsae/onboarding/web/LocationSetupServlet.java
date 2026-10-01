package com.dteam0928.sangsae.onboarding.web;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.common.web.WebUtil;
import com.dteam0928.sangsae.onboarding.dto.LocationSetupRequest;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * O-03 위치 권한 (Step 2/2). 어떤 경우든 마지막에는 온보딩 완료 처리 후 H-00 으로 이동한다.
 * <pre>
 * POST /onboarding/location  action=allow  latitude=35.15&longitude=129.06  (Geolocation 성공)
 * POST /onboarding/location  action=deny   (브라우저 거부/타임아웃/미지원)
 * POST /onboarding/location  action=later  ([나중에 하기])
 * </pre>
 */
@WebServlet(Routes.ONBOARDING_LOCATION)
public class LocationSetupServlet extends BaseServlet {

    private static final String MSG_MANUAL_REGION = "위치 권한 없이도 탐색 화면에서 지역을 직접 선택해 이용할 수 있어요.";

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forward(req, resp, Views.ONBOARDING_LOCATION);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        LoginUser user = loginUser(req);
        String action = param(req, "action");
        LoginUser updated;

        if ("allow".equals(action)) {
            try {
                updated = app().onboardingService().saveLocation(user.getId(),
                        new LocationSetupRequest(true, toDouble(param(req, "latitude")),
                                toDouble(param(req, "longitude"))));
            } catch (BusinessException e) {
                // 좌표가 잘못 와도 홈 진입은 막지 않는다 (화면설계 O-03 정책)
                updated = app().onboardingService().skipLocation(user.getId());
                WebUtil.putFlash(req, MSG_MANUAL_REGION);
            }
        } else {
            updated = app().onboardingService().skipLocation(user.getId());
            if ("deny".equals(action)) WebUtil.putFlash(req, MSG_MANUAL_REGION);
        }

        LoginSession.update(req, updated);
        redirect(req, resp, Routes.HOME);
    }

    private static Double toDouble(String s) {
        try {
            return s == null ? null : Double.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
