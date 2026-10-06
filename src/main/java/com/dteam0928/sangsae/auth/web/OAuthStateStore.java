package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.common.web.Routes;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

/**
 * OAuth state(CSRF 방지값)와 PKCE verifier 를 콜백까지 보관한다.
 *
 * <p>세션 대신 HttpOnly 쿠키를 쓰는 이유: Apple 은 콜백을 다른 사이트에서 POST 로 보내는데,
 * 이때 브라우저가 SameSite=Lax 인 세션 쿠키(JSESSIONID)를 보내지 않아 세션에서 state 를 찾을 수 없다.
 * 그래서 이 쿠키만 SameSite=None; Secure 로 발급한다. (Secure 쿠키는 https 또는 localhost 에서만 저장됨)
 */
public class OAuthStateStore {

    private static final String COOKIE_NAME = "QS_OAUTH";
    private static final int MAX_AGE_SECONDS = 600;

    private final boolean secure;

    /** @param secure 운영/로컬 모두 true 권장. http 로 IP 접속 테스트할 때만 false */
    public OAuthStateStore(boolean secure) {
        this.secure = secure;
    }

    public record Saved(String provider, String state, String codeVerifier) {
    }

    public void save(HttpServletRequest req, HttpServletResponse resp, String provider, String state,
                     String codeVerifier, boolean crossSitePost) {
        String value = provider + "." + state + "." + (codeVerifier == null ? "" : codeVerifier);
        writeCookie(req, resp, value, MAX_AGE_SECONDS, crossSitePost);
    }

    /** 꺼내면서 바로 삭제(1회용). provider 와 state 가 일치할 때만 반환 */
    public Saved consume(HttpServletRequest req, HttpServletResponse resp, String provider, String state) {
        String raw = readCookie(req);
        writeCookie(req, resp, "", 0, true);
        if (raw == null || state == null) return null;

        String[] parts = raw.split("\\.", -1);
        if (parts.length != 3) return null;
        if (!parts[0].equals(provider)) return null;
        if (!MessageDigest.isEqual(parts[1].getBytes(StandardCharsets.US_ASCII),
                state.getBytes(StandardCharsets.US_ASCII))) return null;
        return new Saved(parts[0], parts[1], parts[2].isEmpty() ? null : parts[2]);
    }

    private static String readCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return null;
        for (Cookie c : cookies) if (COOKIE_NAME.equals(c.getName())) return c.getValue();
        return null;
    }

    /** SameSite 속성을 확실히 넣기 위해 Set-Cookie 헤더를 직접 작성 */
    private void writeCookie(HttpServletRequest req, HttpServletResponse resp, String value, int maxAge,
                             boolean crossSitePost) {
        StringBuilder sb = new StringBuilder()
                .append(COOKIE_NAME).append('=').append(value)
                .append("; Path=").append(req.getContextPath()).append(Routes.OAUTH_PREFIX)
                .append("; Max-Age=").append(maxAge)
                .append("; HttpOnly");
        if (secure) sb.append("; Secure");
        // SameSite=None 은 Secure 일 때만 브라우저가 받아준다
        sb.append("; SameSite=").append(crossSitePost && secure ? "None" : "Lax");
        resp.addHeader("Set-Cookie", sb.toString());
    }
}
