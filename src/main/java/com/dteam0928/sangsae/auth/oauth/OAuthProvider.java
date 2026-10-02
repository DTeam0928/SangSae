package com.dteam0928.sangsae.auth.oauth;

import com.dteam0928.sangsae.user.model.AuthProvider;

/**
 * 소셜 로그인 제공자 전략(Strategy).
 *
 * <p>새 제공자 추가 방법 (예: 카카오)
 * <ol>
 *   <li>{@link AuthProvider} 에 KAKAO 추가</li>
 *   <li>이 인터페이스 구현 (보통 {@link AbstractOAuthProvider} 상속)</li>
 *   <li>{@link OAuthProviderRegistry#fromConfig} 에 한 줄 등록</li>
 * </ol>
 * 컨트롤러·서비스·DB 코드는 수정할 필요가 없다.
 */
public interface OAuthProvider {

    AuthProvider type();

    /** PKCE(code_challenge) 사용 여부 */
    boolean usesPkce();

    /**
     * 콜백이 다른 사이트에서 오는 POST 인지 (Apple form_post).
     * true 이면 state 쿠키를 SameSite=None 으로 발급해야 콜백에서 읽을 수 있다.
     */
    default boolean isCrossSitePostCallback() {
        return false;
    }

    /** 사용자를 보낼 제공자 로그인 페이지 URL */
    String authorizationUrl(String state, String codeChallenge);

    /** code → 토큰 교환 → 사용자 정보 조회 */
    OAuthUserInfo authenticate(OAuthCallback callback);
}
