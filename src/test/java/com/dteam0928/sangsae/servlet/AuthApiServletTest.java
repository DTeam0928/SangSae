package com.dteam0928.sangsae.servlet;

import com.dteam0928.sangsae.auth.web.*;
import com.dteam0928.sangsae.testutil.ServletTestHelper;
import com.dteam0928.sangsae.testutil.ServletTestHelper.MockContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthApiServletTest {

    @Test
    @DisplayName("GET /auth/login: 로그인 탭 JSP 포워딩 정상 동작")
    void testLoginServlet_get() throws Exception {
        LoginServlet servlet = new LoginServlet();
        MockContext ctx = new MockContext();
        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertNotNull(ctx.forwardedPath);
        assertTrue(ctx.forwardedPath.contains("auth"));
    }

    @Test
    @DisplayName("GET /auth/signup: 회원가입 탭 JSP 포워딩 정상 동작")
    void testSignupServlet_get() throws Exception {
        SignupServlet servlet = new SignupServlet();
        MockContext ctx = new MockContext();
        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertNotNull(ctx.forwardedPath);
        assertTrue(ctx.forwardedPath.contains("auth"));
    }

    @Test
    @DisplayName("POST /auth/logout: 세션 정리 후 루트(/) 리다이렉트")
    void testLogoutServlet_post() throws Exception {
        LogoutServlet servlet = new LogoutServlet();
        MockContext ctx = new MockContext();
        ctx.method = "POST";
        ServletTestHelper.initServlet(servlet, ctx);
        HttpServletRequest req = ServletTestHelper.createRequest(ctx);
        HttpServletResponse res = ServletTestHelper.createResponse(ctx);

        servlet.service(req, res);

        assertNotNull(ctx.redirectUrl);
        assertTrue(ctx.redirectUrl.endsWith("/sangsae/"));
    }
}
