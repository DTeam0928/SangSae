package com.dteam0928.sangsae.auth.oauth.provider;

import com.fasterxml.jackson.databind.JsonNode;
import com.dteam0928.sangsae.auth.oauth.AbstractOAuthProvider;
import com.dteam0928.sangsae.auth.oauth.OAuthCallback;
import com.dteam0928.sangsae.auth.oauth.OAuthClientConfig;
import com.dteam0928.sangsae.auth.oauth.OAuthUserInfo;
import com.dteam0928.sangsae.user.model.AuthProvider;

import java.util.Map;

/**
 * Google (OpenID Connect).
 * 콘솔: Google Cloud Console → API 및 서비스 → 사용자 인증 정보 → OAuth 클라이언트 ID(웹 애플리케이션)
 * 승인된 리디렉션 URI 에 {app.base-url}/oauth/google/callback 등록.
 */
public class GoogleOAuthProvider extends AbstractOAuthProvider {

    private static final String AUTHORIZE_URL = "https://accounts.google.com/o/oauth2/v2/auth";
    private static final String TOKEN_URL = "https://oauth2.googleapis.com/token";
    private static final String USERINFO_URL = "https://openidconnect.googleapis.com/v1/userinfo";

    public GoogleOAuthProvider(OAuthClientConfig config) {
        super(config);
    }

    @Override
    public AuthProvider type() {
        return AuthProvider.GOOGLE;
    }

    @Override
    public boolean usesPkce() {
        return true;
    }

    @Override
    public String authorizationUrl(String state, String codeChallenge) {
        return AUTHORIZE_URL + "?" + encode(params(
                "response_type", "code",
                "client_id", config.clientId(),
                "redirect_uri", config.redirectUri(),
                "scope", config.scope(),
                "state", state,
                "code_challenge", codeChallenge,
                "code_challenge_method", "S256",
                "prompt", "select_account"));
    }

    @Override
    public OAuthUserInfo authenticate(OAuthCallback cb) {
        JsonNode token = postForm(TOKEN_URL, params(
                "grant_type", "authorization_code",
                "code", cb.code(),
                "client_id", config.clientId(),
                "client_secret", config.clientSecret(),
                "redirect_uri", config.redirectUri(),
                "code_verifier", cb.codeVerifier()), Map.of());

        JsonNode me = getJson(USERINFO_URL, requireText(token, "access_token"));
        // 이메일은 Google 이 검증한 경우에만 사용
        String email = me.path("email_verified").asBoolean(false) ? textOrNull(me, "email") : null;
        return new OAuthUserInfo(type(), requireText(me, "sub"), email,
                textOrNull(me, "name"), textOrNull(me, "picture"));
    }
}
