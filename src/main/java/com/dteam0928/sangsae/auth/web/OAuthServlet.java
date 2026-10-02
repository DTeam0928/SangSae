package com.dteam0928.sangsae.auth.web;

import com.dteam0928.sangsae.auth.model.LoginUser;
import com.dteam0928.sangsae.auth.oauth.OAuthCallback;
import com.dteam0928.sangsae.auth.oauth.OAuthException;
import com.dteam0928.sangsae.auth.oauth.OAuthProvider;
import com.dteam0928.sangsae.auth.oauth.OAuthUserInfo;
import com.dteam0928.sangsae.auth.oauth.Pkce;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.util.RandomUtil;
import com.dteam0928.sangsae.common.web.Routes;
import com.dteam0928.sangsae.common.web.WebUtil;
import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.web.page.BaseServlet;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 소셜 로그인 공통 컨트롤러.
 * <pre>
 * GET       /oauth/{provider}           → 제공자 로그인 페이지로 이동 (O-00 버튼 링크)
 * GET|POST  /oauth/{provider}/callback  → 로그인/가입 처리 후 H-00 또는 O-02 로 이동
 * </pre>
 * provider: google | x | apple. 제공자별 차이는 모두 OAuthProvider 구현체 안에 있다.
 */
@WebServlet(Routes.OAUTH_PREFIX + "/*")
public class OAuthServlet extends BaseServlet {

    private static final Logger log = Logger.getLogger(OAuthServlet.class.getName());

    private transient OAuthStateStore stateStore;

    @Override
    public void init() throws ServletException {
        stateStore = new OAuthStateStore(app().config().getBoolean("oauth.state-cookie.secure", true));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        route(req, resp);
    }

    /** Apple 은 콜백을 POST(form_post)로 보낸다 */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        route(req, resp);
    }

    private void route(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String[] segments = String.valueOf(req.getPathInfo()).replaceAll("^/+", "").split("/");
        String name = segments[0];
        boolean callback = segments.length == 2 && "callback".equals(segments[1]);
        AuthProvider type = AuthProvider.fromPathName(name).filter(AuthProvider::isSocial).orElse(null);

        try {
            if (type == null || segments.length > 2 || (segments.length == 2 && !callback)) {
                throw new BusinessException(ErrorCode.OAUTH_PROVIDER_UNSUPPORTED);
            }
            OAuthProvider provider = app().oAuthProviderRegistry().find(type)
                    .orElseThrow(() -> new BusinessException(ErrorCode.OAUTH_PROVIDER_DISABLED, type.getDisplayName()));

            if (callback) handleCallback(req, resp, provider);
            else if ("GET".equals(req.getMethod())) start(req, resp, provider);
            else resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);

        } catch (BusinessException e) {
            WebUtil.putFlash(req, e.getMessage());
            redirect(req, resp, Routes.LANDING);
        } catch (OAuthException | IllegalArgumentException e) {
            log.log(Level.WARNING, "소셜 로그인 실패: " + name, e);
            WebUtil.putFlash(req, new BusinessException(ErrorCode.OAUTH_FAILED,
                    type == null ? "소셜" : type.getDisplayName()).getMessage());
            redirect(req, resp, Routes.LANDING);
        }
    }

    private void start(HttpServletRequest req, HttpServletResponse resp, OAuthProvider provider) throws IOException {
        String state = RandomUtil.urlSafeToken(24);
        String verifier = provider.usesPkce() ? Pkce.newVerifier() : null;
        stateStore.save(req, resp, provider.type().getPathName(), state, verifier, provider.isCrossSitePostCallback());
        resp.sendRedirect(provider.authorizationUrl(state, verifier == null ? null : Pkce.challenge(verifier)));
    }

    private void handleCallback(HttpServletRequest req, HttpServletResponse resp, OAuthProvider provider)
            throws IOException {
        String displayName = provider.type().getDisplayName();
        OAuthStateStore.Saved saved = stateStore.consume(req, resp, provider.type().getPathName(),
                param(req, "state"));

        // 사용자가 제공자 화면에서 취소한 경우 (Google/X: access_denied, Apple: user_cancelled_authorize)
        if (param(req, "error") != null) throw new BusinessException(ErrorCode.OAUTH_CANCELED, displayName);
        if (saved == null) throw new BusinessException(ErrorCode.OAUTH_STATE_MISMATCH);
        String code = param(req, "code");
        if (code == null || code.isBlank()) throw new BusinessException(ErrorCode.OAUTH_STATE_MISMATCH);

        Map<String, String> params = new HashMap<>();
        req.getParameterMap().forEach((k, v) -> params.put(k, v.length > 0 ? v[0] : null));

        OAuthUserInfo info = provider.authenticate(new OAuthCallback(code, saved.codeVerifier(), params));
        LoginUser user = app().oAuthLoginService().login(info);
        LoginSession.login(req, user);
        redirect(req, resp, Routes.afterLogin(user));   // 신규 가입자는 온보딩 미완료 → O-02
    }
}
