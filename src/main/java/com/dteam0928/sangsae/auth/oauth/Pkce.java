package com.dteam0928.sangsae.auth.oauth;

import com.dteam0928.sangsae.common.util.RandomUtil;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/** PKCE (RFC 7636) S256 */
public final class Pkce {

    private Pkce() {
    }

    public static String newVerifier() {
        return RandomUtil.urlSafeToken(32);   // 43자
    }

    public static String challenge(String verifier) {
        try {
            byte[] hash = MessageDigest.getInstance("SHA-256").digest(verifier.getBytes(StandardCharsets.US_ASCII));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
