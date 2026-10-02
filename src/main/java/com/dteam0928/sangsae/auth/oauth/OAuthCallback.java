package com.dteam0928.sangsae.auth.oauth;

import java.util.Map;

/**
 * 콜백으로 받은 값.
 * @param code         authorization code
 * @param codeVerifier PKCE 원문 (PKCE 를 안 쓰는 제공자는 null)
 * @param params       콜백 요청의 전체 파라미터 (Apple 의 user 등 제공자별 추가값)
 */
public record OAuthCallback(String code, String codeVerifier, Map<String, String> params) {
}
