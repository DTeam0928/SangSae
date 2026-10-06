package com.dteam0928.sangsae.servlet;

import com.dteam0928.sangsae.AppContext;
import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.common.config.AppConfig;
import com.dteam0928.sangsae.testutil.ServletTestHelper;
import com.dteam0928.sangsae.testutil.ServletTestHelper.MockContext;
import com.dteam0928.sangsae.user.exp.ExpPolicy;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import com.dteam0928.sangsae.user.exp.LevelInfo;
import com.dteam0928.sangsae.user.exp.servlet.UserExperienceApiServlet;
import com.dteam0928.sangsae.user.model.AuthProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class UserExperienceApiServletTest {

    @Test
    @DisplayName("GET /api/user/experience: 비로그인 상태 접근 시 401 Unauthorized 차단")
    void testExperienceServlet_unauthorized() throws Exception {
        UserExperienceApiServlet servlet = new UserExperienceApiServlet();
        MockContext ctx = new MockContext();
        ctx.method = "GET";
        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(401, ctx.status);
        assertTrue(ctx.responseBody.toString().contains("로그인이 필요합니다"));
    }

    @Test
    @DisplayName("GET /api/user/experience: 로그인 상태 정상 조회 시 200 OK 및 레벨/칭호/진행률 JSON 반환")
    void testExperienceServlet_success() throws Exception {
        UserExperienceApiServlet servlet = new UserExperienceApiServlet();
        MockContext ctx = new MockContext();
        ctx.method = "GET";

        // 로그인 세션 모킹
        LoginUser loginUser = new LoginUser(1L, "test@sangsae.com", "길동이", AuthProvider.LOCAL, true);
        ctx.sessionAttributes.put(com.dteam0928.sangsae.common.web.SessionKeys.LOGIN_USER, loginUser);

        // AppContext 모킹
        AppContext mockApp = createMockAppContext();
        ctx.contextAttributes.put(AppContext.ATTRIBUTE, mockApp);

        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(200, ctx.status);
        String body = ctx.responseBody.toString();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"level\":"));
        assertTrue(body.contains("\"title\":"));
        assertTrue(body.contains("\"totalXp\":"));
        assertTrue(body.contains("\"progressPercent\":"));
    }

    private AppContext createMockAppContext() throws Exception {
        Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        AppContext app = (AppContext) unsafe.allocateInstance(AppContext.class);
        ExpPolicy policy = new ExpPolicy();
        ExperienceService expService = new ExperienceService(
                new com.dteam0928.sangsae.common.db.TransactionTemplate(null),
                new com.dteam0928.sangsae.user.dao.UserProfileDao() {
                    @Override public void insert(jakarta.persistence.EntityManager em, com.dteam0928.sangsae.user.model.UserProfile profile) {}
                    @Override public java.util.Optional<com.dteam0928.sangsae.user.model.UserProfile> findByUserId(jakarta.persistence.EntityManager em, long userId) { return java.util.Optional.empty(); }
                    @Override public boolean existsByNickname(jakarta.persistence.EntityManager em, String nickname) { return false; }
                    @Override public void updateNickname(jakarta.persistence.EntityManager em, long userId, String nickname) {}
                    @Override public void updateLocation(jakarta.persistence.EntityManager em, long userId, boolean permitted, com.dteam0928.sangsae.common.geo.GeoPoint location) {}
                },
                policy
        ) {
            @Override
            public LevelInfo getUserLevelInfo(Long userId) {
                return new LevelInfo(3, "새싹 탐험가", 2500, 500, 500, 50.0, false);
            }
        };

        setField(app, "experienceService", expService);
        setField(app, "expPolicy", policy);
        setField(app, "config", new AppConfig(new Properties()));
        return app;
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = AppContext.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
