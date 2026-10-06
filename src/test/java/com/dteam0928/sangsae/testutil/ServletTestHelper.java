package com.dteam0928.sangsae.testutil;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.*;
import java.lang.reflect.Proxy;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * 서블릿 API 전수 테스트를 위한 경량 Jakarta Mock 생성 도구
 */
public class ServletTestHelper {

    public static class MockContext {
        public final Map<String, String> params = new HashMap<>();
        public final Map<String, Object> attributes = new HashMap<>();
        public final Map<String, Object> sessionAttributes = new HashMap<>();
        public final Map<String, Object> contextAttributes = new HashMap<>();
        public final StringWriter responseBody = new StringWriter();
        public int status = 200;
        public String redirectUrl = null;
        public String forwardedPath = null;
        public String requestBody = "";
        public String method = "GET";
        public boolean sessionInvalidated = false;
    }

    public static void initServlet(jakarta.servlet.http.HttpServlet servlet, MockContext ctx) {
        ServletContext sc = (ServletContext) Proxy.newProxyInstance(
                ServletContext.class.getClassLoader(),
                new Class<?>[]{ServletContext.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    if ("getAttribute".equals(m)) {
                        return ctx.contextAttributes.get(args[0]);
                    } else if ("setAttribute".equals(m)) {
                        ctx.contextAttributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("getContextPath".equals(m)) {
                        return "/sangsae";
                    }
                    return null;
                }
        );

        jakarta.servlet.ServletConfig config = (jakarta.servlet.ServletConfig) Proxy.newProxyInstance(
                jakarta.servlet.ServletConfig.class.getClassLoader(),
                new Class<?>[]{jakarta.servlet.ServletConfig.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    if ("getServletContext".equals(m)) {
                        return sc;
                    } else if ("getServletName".equals(m)) {
                        return servlet.getClass().getSimpleName();
                    }
                    return null;
                }
        );

        try {
            servlet.init(config);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static HttpServletRequest createRequest(MockContext ctx) {
        HttpSession session = (HttpSession) Proxy.newProxyInstance(
                HttpSession.class.getClassLoader(),
                new Class<?>[]{HttpSession.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    if ("getAttribute".equals(m)) {
                        return ctx.sessionAttributes.get(args[0]);
                    } else if ("setAttribute".equals(m)) {
                        ctx.sessionAttributes.put((String) args[0], args[1]);
                        return null;
                    } else if ("removeAttribute".equals(m)) {
                        ctx.sessionAttributes.remove(args[0]);
                        return null;
                    } else if ("invalidate".equals(m)) {
                        ctx.sessionInvalidated = true;
                        ctx.sessionAttributes.clear();
                        return null;
                    }
                    return null;
                }
        );

        RequestDispatcher dispatcher = (RequestDispatcher) Proxy.newProxyInstance(
                RequestDispatcher.class.getClassLoader(),
                new Class<?>[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    return null;
                }
        );

        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    switch (m) {
                        case "getParameter":
                            return ctx.params.get(args[0]);
                        case "getParameterValues":
                            String val = ctx.params.get(args[0]);
                            return val != null ? new String[]{val} : null;
                        case "getAttribute":
                            return ctx.attributes.get(args[0]);
                        case "setAttribute":
                            ctx.attributes.put((String) args[0], args[1]);
                            return null;
                        case "getSession":
                            if (args != null && args.length > 0 && Boolean.FALSE.equals(args[0])) {
                                return ctx.sessionInvalidated ? null : session;
                            }
                            return session;
                        case "changeSessionId":
                            return "new-session-id";
                        case "getContextPath":
                            return "/sangsae";
                        case "getRequestURI":
                            return "/sangsae/test";
                        case "getMethod":
                            return ctx.method;
                        case "getReader":
                            return new BufferedReader(new StringReader(ctx.requestBody));
                        case "getInputStream":
                            return new DelegatingServletInputStream(new ByteArrayInputStream(ctx.requestBody.getBytes(StandardCharsets.UTF_8)));
                        case "getRequestDispatcher":
                            ctx.forwardedPath = (String) args[0];
                            return dispatcher;
                        case "setCharacterEncoding":
                            return null;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    public static HttpServletResponse createResponse(MockContext ctx) {
        PrintWriter writer = new PrintWriter(ctx.responseBody);
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    String m = method.getName();
                    switch (m) {
                        case "getWriter":
                            return writer;
                        case "setStatus":
                            ctx.status = (int) args[0];
                            return null;
                        case "getStatus":
                            return ctx.status;
                        case "sendError":
                            ctx.status = (int) args[0];
                            return null;
                        case "sendRedirect":
                            ctx.redirectUrl = (String) args[0];
                            return null;
                        case "setContentType":
                        case "setCharacterEncoding":
                        case "setHeader":
                        case "addCookie":
                            return null;
                        default:
                            return defaultValue(method.getReturnType());
                    }
                }
        );
    }

    private static Object defaultValue(Class<?> returnType) {
        if (returnType == void.class) return null;
        if (returnType == boolean.class) return false;
        if (returnType == int.class) return 0;
        if (returnType == long.class) return 0L;
        if (returnType == double.class) return 0.0;
        return null;
    }

    private static class DelegatingServletInputStream extends jakarta.servlet.ServletInputStream {
        private final InputStream sourceStream;

        public DelegatingServletInputStream(InputStream sourceStream) {
            this.sourceStream = sourceStream;
        }

        @Override
        public boolean isFinished() {
            try {
                return sourceStream.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(jakarta.servlet.ReadListener readListener) {}

        @Override
        public int read() throws IOException {
            return sourceStream.read();
        }
    }
}
