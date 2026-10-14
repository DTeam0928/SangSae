package com.dteam0928.sangsae.mypage.web;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.validation.Validators;
import com.dteam0928.sangsae.common.web.ApiResponse;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.WebUtil;
import com.dteam0928.sangsae.mypage.dto.MyProfile;
import com.dteam0928.sangsae.onboarding.dto.LocationSetupRequest;
import com.dteam0928.sangsae.onboarding.dto.ProfileForm;
import com.dteam0928.sangsae.onboarding.dto.ProfileSetupRequest;
import com.dteam0928.sangsae.onboarding.service.OnboardingService;
import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 마이페이지 — 가입 후 닉네임·관심사·위치 변경 API (화면의 카드마다 [변경]/[저장] 버튼이 따로 있다)
 * <pre>
 * GET  /api/me/profile     현재 닉네임 + 관심사 칩(선택 여부) + 위치 허용 여부
 * POST /api/me/nickname    nickname=..                          닉네임만 변경
 * POST /api/me/interests   interestIds=1&amp;interestIds=3        관심사만 변경 (0개 = 전부 해제)
 * POST /api/me/location    latitude=35.15&amp;longitude=129.03   위치 허용 + 현재 좌표 저장
 * POST /api/me/profile     nickname=..&amp;interestIds=..         닉네임·관심사 한 번에 (O-02 와 같은 형식)
 * </pre>
 * 검증·저장은 O-02/O-03 의 {@link OnboardingService} 를 그대로 쓴다
 * (닉네임 1~30자·중복 불가, 관심사 0~5개, 좌표 범위 검사).
 * 저장하면 세션 정보도 바꿔서 다른 화면에 표시되는 닉네임이 바로 바뀐다.
 * 응답은 모두 GET 과 같은 최신 프로필.
 */

@WebServlet(Routes.API_ME + "/*")
public class MyProfileApiServlet extends BaseServlet {

    private static final Logger log = Logger.getLogger(MyProfileApiServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        if (!"/profile".equals(req.getPathInfo())) {
            WebUtil.writeJson(resp, 404, ApiResponse.fail(ErrorCode.NOT_FOUND));
            return;
        }
        respond(resp, () -> ApiResponse.ok(null, profile(currentUser(req))));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String path = req.getPathInfo() == null ? "" : req.getPathInfo();
        switch (path) {
            case "/nickname" -> respond(resp, () -> {
                LoginUser user = currentUser(req);
                ProfileForm current = onboarding().getProfileForm(user.getId());
                saveProfile(req, user, param(req, "nickname"), current.getSelectedIds());
                return ApiResponse.ok("닉네임을 변경했습니다.", profile(user));
            });
            case "/interests" -> respond(resp, () -> {
                LoginUser user = currentUser(req);
                ProfileForm current = onboarding().getProfileForm(user.getId());
                saveProfile(req, user, current.getNickname(), parseIds(req.getParameterValues("interestIds")));
                return ApiResponse.ok("관심사를 저장했습니다.", profile(user));
            });
            case "/profile" -> respond(resp, () -> {
                LoginUser user = currentUser(req);
                saveProfile(req, user, param(req, "nickname"), parseIds(req.getParameterValues("interestIds")));
                return ApiResponse.ok("저장했습니다.", profile(user));
            });
            case "/location" -> respond(resp, () -> {
                LoginUser user = currentUser(req);
                LoginUser updated = onboarding().saveLocation(user.getId(), new LocationSetupRequest(true,
                        toDouble(param(req, "latitude")), toDouble(param(req, "longitude"))));
                LoginSession.update(req, updated);
                return ApiResponse.ok("현재 위치를 저장했습니다.", profile(updated));
            });
            default -> WebUtil.writeJson(resp, 404, ApiResponse.fail(ErrorCode.NOT_FOUND));
        }
    }

    /** 닉네임·관심사 저장 + 세션 닉네임 갱신 */
    private void saveProfile(HttpServletRequest req, LoginUser user, String nickname, List<Short> interestIds) {
        String saved = onboarding().saveProfile(user.getId(), new ProfileSetupRequest(nickname, interestIds));
        user.setNickname(saved);
        LoginSession.update(req, user);
    }

    private MyProfile profile(LoginUser user) {
        ProfileForm form = onboarding().getProfileForm(user.getId());
        List<Short> selected = form.getSelectedIds();
        List<MyProfile.Interest> interests = form.getCategories().stream()
                .map(c -> new MyProfile.Interest(c.getId(), c.getCode(), c.getName(), selected.contains(c.getId())))
                .toList();
        AuthProvider provider = user.getProvider();
        return new MyProfile(user.getEmail(),
                provider == null ? null : provider.name(),
                provider == null ? null : provider.getDisplayName(),
                form.getNickname(), Validators.NICKNAME_MAX_LENGTH,
                interests, selected, form.getMaxInterestCount(),
                onboarding().isLocationPermitted(user.getId()));
    }

    private OnboardingService onboarding() {
        return app().onboardingService();
    }

    /** AuthFilter 가 먼저 막지만 안전하게 한 번 더 확인 */
    private LoginUser currentUser(HttpServletRequest req) {
        LoginUser user = loginUser(req);
        if (user == null) throw new BusinessException(ErrorCode.UNAUTHORIZED);
        return user;
    }

    private void respond(HttpServletResponse resp, Supplier<ApiResponse<?>> action) throws IOException {
        try {
            WebUtil.writeJson(resp, 200, action.get());
        } catch (BusinessException e) {
            WebUtil.writeJson(resp, e.getErrorCode().getStatus(), ApiResponse.fail(e));
        } catch (RuntimeException e) {
            log.log(Level.SEVERE, "마이페이지 API 처리 실패", e);
            WebUtil.writeJson(resp, 500, ApiResponse.fail(ErrorCode.INTERNAL_ERROR));
        }
    }

    /** interestIds 값들 → id 목록. 빈 값은 건너뛰고, 숫자가 아니면 INTEREST_INVALID */
    private static List<Short> parseIds(String[] values) {
        List<Short> ids = new ArrayList<>();
        if (values == null) return ids;
        for (String v : values) {
            if (v == null || v.isBlank()) continue;
            try {
                ids.add(Short.valueOf(v.trim()));
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INTEREST_INVALID);
            }
        }
        return ids;
    }

    private static Double toDouble(String s) {
        try {
            return s == null || s.isBlank() ? null : Double.valueOf(s.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
