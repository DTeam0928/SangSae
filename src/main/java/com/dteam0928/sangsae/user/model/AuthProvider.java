package com.dteam0928.sangsae.user.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * 로그인 수단. users.oauth_provider 컬럼에 name() 으로 저장된다.
 * 새 소셜 로그인(예: KAKAO)을 추가할 때는 여기에 한 줄 추가 + OAuthProvider 구현 클래스 1개 작성.
 */
public enum AuthProvider {

    LOCAL("local", "이메일"),
    GOOGLE("google", "Google"),
    X("x", "X"),
    APPLE("apple", "Apple");

    /** URL 에 쓰는 이름: /oauth/google */
    private final String pathName;
    /** 안내 문구에 쓰는 이름 */
    private final String displayName;

    AuthProvider(String pathName, String displayName) {
        this.pathName = pathName;
        this.displayName = displayName;
    }

    public String getPathName() {
        return pathName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isSocial() {
        return this != LOCAL;
    }

    public static Optional<AuthProvider> fromPathName(String pathName) {
        return Arrays.stream(values()).filter(p -> p.pathName.equalsIgnoreCase(pathName)).findFirst();
    }
}
