package com.dteam0928.sangsae.common.db;

import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * UNIQUE/PK 제약 위반(MySQL 1062).
 * 어느 키가 위반됐는지 {@link #isKey(String)} 로 구분해서 이메일 중복/닉네임 중복 등을 판단한다.
 */
public class DuplicateKeyException extends DataAccessException {

    public static final int MYSQL_DUPLICATE_ENTRY = 1062;
    // 예: Duplicate entry 'a@b.com' for key 'users.uk_users_email'
    private static final Pattern KEY_PATTERN = Pattern.compile("for key '([^']+)'");

    private final String keyName;

    public DuplicateKeyException(SQLException cause) {
        super(cause.getMessage(), cause);
        Matcher m = KEY_PATTERN.matcher(String.valueOf(cause.getMessage()));
        this.keyName = m.find() ? m.group(1) : "";
    }

    public String getKeyName() {
        return keyName;
    }

    /** 제약 이름으로 비교 (테이블 접두어 유무와 무관하게 동작) */
    public boolean isKey(String constraintName) {
        return keyName.equals(constraintName) || keyName.endsWith("." + constraintName);
    }
}
