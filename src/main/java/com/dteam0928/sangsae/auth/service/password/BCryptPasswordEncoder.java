package com.dteam0928.sangsae.auth.service.password;

import org.mindrot.jbcrypt.BCrypt;

public class BCryptPasswordEncoder implements PasswordEncoder {

    /** 해시 강도. 1 올릴 때마다 계산 시간이 2배 (10 이면 약 0.1초 내외) */
    private final int cost;

    public BCryptPasswordEncoder(int cost) {
        if (cost < 4 || cost > 31) throw new IllegalArgumentException("BCrypt cost 는 4~31: " + cost);
        this.cost = cost;
    }

    @Override
    public String encode(String rawPassword) {
        return BCrypt.hashpw(rawPassword, BCrypt.gensalt(cost));
    }

    @Override
    public boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null || !encodedPassword.startsWith("$2")) return false;
        try {
            return BCrypt.checkpw(rawPassword, encodedPassword);
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
