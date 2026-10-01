package com.dteam0928.sangsae.common.validation;

import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.common.util.Strings;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * 입력값 규칙 모음. 정책이 바뀌면 이 파일의 상수만 고치면 된다.
 * 각 메서드는 검증에 실패하면 {@link BusinessException} 을 던지고, 성공하면 정규화된 값을 돌려준다.
 */
public final class Validators {

    private Validators() {
    }

    // ---- 이메일 (화면설계 O-01) ----
    public static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static final int EMAIL_MAX_LENGTH = 255;

    // ---- 비밀번호 (화면설계 O-01: 영문+숫자 포함 8자 이상, 특수문자 허용) ----
    public static final int PASSWORD_MIN_LENGTH = 8;
    /** BCrypt 는 72바이트까지만 사용하므로 상한을 둔다 */
    public static final int PASSWORD_MAX_LENGTH = 64;
    private static final Pattern PASSWORD_LETTER = Pattern.compile("[A-Za-z]");
    private static final Pattern PASSWORD_DIGIT = Pattern.compile("[0-9]");

    // ---- 닉네임 (화면설계 O-02: trim 후 1~30자) ----
    public static final int NICKNAME_MIN_LENGTH = 1;
    public static final int NICKNAME_MAX_LENGTH = 30;

    // ---- 관심사 (화면설계 O-02: 1~5개) ----
    public static final int INTEREST_MIN_COUNT = 1;
    public static final int INTEREST_MAX_COUNT = 5;

    /** 이메일 형식 검사 후 소문자로 정규화 */
    public static String email(String raw) {
        String email = Strings.trimToEmpty(raw).toLowerCase(Locale.ROOT);
        if (email.isEmpty()) throw new BusinessException(ErrorCode.EMAIL_REQUIRED);
        if (email.length() > EMAIL_MAX_LENGTH || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ErrorCode.EMAIL_INVALID);
        }
        return email;
    }

    /** 로그인용: 비밀번호가 비어있지만 않으면 통과 */
    public static String passwordPresent(String raw) {
        if (raw == null || raw.isEmpty()) throw new BusinessException(ErrorCode.PASSWORD_REQUIRED);
        return raw;
    }

    /** 가입용: 비밀번호 규칙 검사 (비밀번호는 trim 하지 않는다) */
    public static String newPassword(String raw) {
        passwordPresent(raw);
        int len = Strings.length(raw);
        boolean ok = len >= PASSWORD_MIN_LENGTH && len <= PASSWORD_MAX_LENGTH
                && PASSWORD_LETTER.matcher(raw).find()
                && PASSWORD_DIGIT.matcher(raw).find()
                && raw.chars().noneMatch(Character::isWhitespace);
        if (!ok) throw new BusinessException(ErrorCode.PASSWORD_POLICY, PASSWORD_MIN_LENGTH, PASSWORD_MAX_LENGTH);
        return raw;
    }

    public static void passwordConfirm(String password, String confirm) {
        if (confirm == null || !confirm.equals(password)) throw new BusinessException(ErrorCode.PASSWORD_MISMATCH);
    }

    /** 닉네임 trim 후 길이 검사 */
    public static String nickname(String raw) {
        String nickname = Strings.trimToEmpty(raw);
        if (nickname.isEmpty()) throw new BusinessException(ErrorCode.NICKNAME_REQUIRED);
        int len = Strings.length(nickname);
        if (len < NICKNAME_MIN_LENGTH || len > NICKNAME_MAX_LENGTH) {
            throw new BusinessException(ErrorCode.NICKNAME_LENGTH, NICKNAME_MIN_LENGTH, NICKNAME_MAX_LENGTH);
        }
        return nickname;
    }
}
