package com.dteam0928.sangsae.servlet;

import com.dteam0928.sangsae.quest.servlet.*;
import com.dteam0928.sangsae.testutil.ServletTestHelper;
import com.dteam0928.sangsae.testutil.ServletTestHelper.MockContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class QuestApiServletTest {

    @Test
    @DisplayName("POST /user-quests/complete: 비로그인 상태 접근 시 401 Unauthorized 차단")
    void testQuestCompleteServlet_unauthorized() throws Exception {
        QuestCompleteServlet servlet = new QuestCompleteServlet();
        MockContext ctx = new MockContext();
        ctx.method = "POST";
        ctx.params.put("userQuestId", "1");
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(401, ctx.status);
    }

    @Test
    @DisplayName("POST /user-quests/complete: Accept가 application/json일 때 200 OK 및 경험치 보상 JSON 반환")
    void testQuestCompleteServlet_jsonSuccess() throws Exception {
        QuestCompleteServlet servlet = new QuestCompleteServlet();
        MockContext ctx = new MockContext();
        ctx.method = "POST";
        ctx.params.put("userQuestId", "1");
        ctx.headers.put("Accept", "application/json");

        // 로그인 세션 모킹
        com.dteam0928.sangsae.auth.model.LoginUser loginUser = new com.dteam0928.sangsae.auth.model.LoginUser(
                1L, "test@sangsae.com", "길동이", com.dteam0928.sangsae.user.model.AuthProvider.LOCAL, true
        );
        ctx.sessionAttributes.put(com.dteam0928.sangsae.common.web.SessionKeys.LOGIN_USER, loginUser);

        // AppContext 모킹
        com.dteam0928.sangsae.AppContext mockApp = createMockAppContextWithQuestService(true);
        ctx.contextAttributes.put(com.dteam0928.sangsae.AppContext.ATTRIBUTE, mockApp);

        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(200, ctx.status);
        String body = ctx.responseBody.toString();
        assertTrue(body.contains("\"success\":true"));
        assertTrue(body.contains("\"earnedXp\":500"));
        assertTrue(body.contains("\"reward\":"));
        assertTrue(body.contains("\"isLevelUp\":true"));
    }

    @Test
    @DisplayName("POST /user-quests/complete: 일반 Form 요청 시 퀘스트 목록으로 리다이렉트")
    void testQuestCompleteServlet_formRedirect() throws Exception {
        QuestCompleteServlet servlet = new QuestCompleteServlet();
        MockContext ctx = new MockContext();
        ctx.method = "POST";
        ctx.params.put("userQuestId", "1");

        com.dteam0928.sangsae.auth.model.LoginUser loginUser = new com.dteam0928.sangsae.auth.model.LoginUser(
                1L, "test@sangsae.com", "길동이", com.dteam0928.sangsae.user.model.AuthProvider.LOCAL, true
        );
        ctx.sessionAttributes.put(com.dteam0928.sangsae.common.web.SessionKeys.LOGIN_USER, loginUser);

        com.dteam0928.sangsae.AppContext mockApp = createMockAppContextWithQuestService(true);
        ctx.contextAttributes.put(com.dteam0928.sangsae.AppContext.ATTRIBUTE, mockApp);

        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertNotNull(ctx.redirectUrl);
        assertTrue(ctx.redirectUrl.endsWith("/user-quests"));
    }

    @Test
    @DisplayName("POST /user-quests/schedule/update: 비로그인 상태 접근 시 401 Unauthorized 차단")
    void testQuestScheduleUpdateServlet_unauthorized() throws Exception {
        QuestScheduleUpdateServlet servlet = new QuestScheduleUpdateServlet();
        MockContext ctx = new MockContext();
        ctx.method = "POST";
        ctx.params.put("userQuestId", "1");
        ctx.params.put("scheduledDate", "2026-10-10");
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(401, ctx.status);
    }

    @Test
    @DisplayName("GET /quest-logs: 비로그인 상태 접근 시 401 Unauthorized 차단")
    void testQuestLogServlet_unauthorized() throws Exception {
        QuestLogServlet servlet = new QuestLogServlet();
        MockContext ctx = new MockContext();
        ctx.method = "GET";
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(401, ctx.status);
    }

    @Test
    @DisplayName("GET /quest-bookmarks: 비로그인 상태 접근 시 401 Unauthorized 차단")
    void testQuestBookmarkServlet_unauthorized() throws Exception {
        QuestBookmarkServlet servlet = new QuestBookmarkServlet();
        MockContext ctx = new MockContext();
        ctx.method = "GET";
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertEquals(401, ctx.status);
    }

    private com.dteam0928.sangsae.AppContext createMockAppContextWithQuestService(boolean success) throws Exception {
        java.lang.reflect.Field unsafeField = sun.misc.Unsafe.class.getDeclaredField("theUnsafe");
        unsafeField.setAccessible(true);
        sun.misc.Unsafe unsafe = (sun.misc.Unsafe) unsafeField.get(null);
        com.dteam0928.sangsae.AppContext app = (com.dteam0928.sangsae.AppContext) unsafe.allocateInstance(com.dteam0928.sangsae.AppContext.class);

        com.dteam0928.sangsae.quest.service.QuestService mockQuestService = new com.dteam0928.sangsae.quest.service.QuestService(
                new com.dteam0928.sangsae.common.db.TransactionTemplate(null), null, null
        ) {
            @Override
            public com.dteam0928.sangsae.quest.dto.QuestCompleteResult completeQuestWithReward(
                    Long userQuestId, Long userId, String proofImageUrl, String proofText, Double userLat, Double userLng) {
                if (!success) return null;
                com.dteam0928.sangsae.quest.dto.QuestLog log = new com.dteam0928.sangsae.quest.dto.QuestLog();
                log.setId(10L);
                log.setEarnedXp(500);

                com.dteam0928.sangsae.user.exp.LevelInfo prevLevel = new com.dteam0928.sangsae.user.exp.LevelInfo(1, "새싹 탐험가", 500, 500, 500, 50.0, false);
                com.dteam0928.sangsae.user.exp.LevelInfo currLevel = new com.dteam0928.sangsae.user.exp.LevelInfo(2, "새싹 탐험가", 1000, 0, 1000, 0.0, false);
                com.dteam0928.sangsae.user.exp.ExpRewardResult reward = com.dteam0928.sangsae.user.exp.ExpRewardResult.of(500, 500, 1000, prevLevel, currLevel);
                return new com.dteam0928.sangsae.quest.dto.QuestCompleteResult(log, reward);
            }
        };

        java.lang.reflect.Field f = com.dteam0928.sangsae.AppContext.class.getDeclaredField("questService");
        f.setAccessible(true);
        f.set(app, mockQuestService);
        return app;
    }
}
