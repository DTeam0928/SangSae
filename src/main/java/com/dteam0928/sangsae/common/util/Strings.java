package com.dteam0928.sangsae.common.util;

public final class Strings {

    private Strings() {
    }

    public static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    /** 앞뒤 공백 제거. null 은 빈 문자열로 */
    public static String trimToEmpty(String s) {
        return s == null ? "" : s.strip();
    }

    public static String trimToNull(String s) {
        if (s == null) return null;
        String t = s.strip();
        return t.isEmpty() ? null : t;
    }

    /** 한글·이모지 포함 실제 글자 수 (MySQL VARCHAR(n) 의 n 과 같은 기준) */
    public static int length(String s) {
        return s == null ? 0 : s.codePointCount(0, s.length());
    }
}
