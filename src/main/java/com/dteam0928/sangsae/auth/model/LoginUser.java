package com.dteam0928.sangsae.auth.model;

import com.dteam0928.sangsae.user.model.AuthProvider;

import java.io.Serializable;

/**
 * 세션에 보관하는 로그인 사용자 정보.
 * 비밀번호 해시 같은 민감 정보는 넣지 않는다. JSP 에서 ${sessionScope.loginUser.nickname} 으로 사용.
 */
public class LoginUser implements Serializable {

    private static final long serialVersionUID = 1L;

    private long id;
    private String email;
    private String nickname;
    private AuthProvider provider;
    private boolean onboardingCompleted;

    public LoginUser() {
    }

    public LoginUser(long id, String email, String nickname, AuthProvider provider, boolean onboardingCompleted) {
        this.id = id;
        this.email = email;
        this.nickname = nickname;
        this.provider = provider;
        this.onboardingCompleted = onboardingCompleted;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public AuthProvider getProvider() { return provider; }
    public void setProvider(AuthProvider provider) { this.provider = provider; }

    public boolean isOnboardingCompleted() { return onboardingCompleted; }
    public void setOnboardingCompleted(boolean onboardingCompleted) { this.onboardingCompleted = onboardingCompleted; }
}
