package com.dteam0928.sangsae.common.util;

import java.security.SecureRandom;
import java.util.Base64;

public final class RandomUtil {

    private static final SecureRandom RANDOM = new SecureRandom();

    private RandomUtil() {
    }

    /** URL 에 그대로 쓸 수 있는 난수 문자열 (state, PKCE verifier 등) */
    public static String urlSafeToken(int byteLength) {
        byte[] bytes = new byte[byteLength];
        RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public static int nextInt(int bound) {
        return RANDOM.nextInt(bound);
    }
}
