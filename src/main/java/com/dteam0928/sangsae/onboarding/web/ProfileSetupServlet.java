package com.dteam0928.sangsae.onboarding.web;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.web.LoginSession;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.Views;
import com.dteam0928.sangsae.onboarding.dto.ProfileForm;
import com.dteam0928.sangsae.onboarding.dto.ProfileSetupRequest;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * O-02 프로필·관심사 설정 (Step 1/2)
 * <pre>
 * GET  /onboarding/profile
 * POST /onboarding/profile  action=next  nickname=..&interestIds=1&interestIds=3
 * POST /onboarding/profile  action=skip  (검증 없이 O-03)
 * </pre>
 */
@WebServlet(Routes.ONBOARDING_PROFILE)
public class ProfileSetupServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("form", app().onboardingService().getProfileForm(loginUser(req).getId()));
        forward(req, resp, Views.ONBOARDING_PROFILE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        LoginUser user = loginUser(req);

        ProfileSetupRequest form = new ProfileSetupRequest(param(req, "nickname"), null);
        try {
            form.setInterestIds(parseIds(req.getParameterValues("interestIds")));
            String nickname = app().onboardingService().saveProfile(user.getId(), form);
            user.setNickname(nickname);
            LoginSession.update(req, user);
            redirect(req, resp, Routes.ONBOARDING_LOCATION);
        } catch (BusinessException e) {
            // 입력한 값을 유지한 채 다시 보여준다
            ProfileForm view = app().onboardingService().getProfileForm(user.getId());
            view.setNickname(form.getNickname());
            view.setSelectedIds(form.getInterestIds());
            req.setAttribute("form", view);
            forwardWithError(req, resp, Views.ONBOARDING_PROFILE, e);
        }
    }

    private static List<Short> parseIds(String[] values) {
        List<Short> ids = new ArrayList<>();
        if (values == null) return ids;
        for (String v : values) {
            try {
                ids.add(Short.valueOf(v.trim()));
            } catch (NumberFormatException e) {
                throw new BusinessException(ErrorCode.INTEREST_INVALID);
            }
        }
        return ids;
    }
}
