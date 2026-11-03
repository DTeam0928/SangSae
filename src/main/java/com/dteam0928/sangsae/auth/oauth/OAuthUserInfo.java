package com.dteam0928.sangsae.auth.oauth;

import com.dteam0928.sangsae.user.model.AuthProvider;

/** 소셜 제공자에서 받아온 사용자 정보를 공통 형식으로 정리한 것 */
public class OAuthUserInfo {

    private AuthProvider provider;
    /** 제공자 고유 식별자 (Google/Apple: sub, X: id) → users.oauth_id */
    private String providerUserId;
    /** 검증된 이메일만 넣는다. 없거나 미검증이면 null */
    private String email;
    private String displayName;
    private String profileImageUrl;

    public OAuthUserInfo() {
    }

    public OAuthUserInfo(AuthProvider provider, String providerUserId, String email, String displayName,
                         String profileImageUrl) {
        this.provider = provider;
        this.providerUserId = providerUserId;
        this.email = email;
        this.displayName = displayName;
        this.profileImageUrl = profileImageUrl;
    }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public String getProviderUserId() { return providerUserId; }
    public void setProviderUserId(String providerUserId) { this.providerUserId = providerUserId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }
}
