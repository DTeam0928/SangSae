package com.dteam0928.sangsae.auth.service.password;

/** 비밀번호 해시. 알고리즘을 바꿔도(BCrypt → Argon2 등) Service 코드는 그대로 둔다. */
public interface PasswordEncoder {

    String encode(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
