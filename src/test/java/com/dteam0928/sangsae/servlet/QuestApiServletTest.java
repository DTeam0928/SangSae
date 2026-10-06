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
}
