package com.dteam0928.sangsae.auth.oauth;

/** 제공자별 클라이언트 설정 */
public record OAuthClientConfig(String clientId, String clientSecret, String redirectUri, String scope) {
}
