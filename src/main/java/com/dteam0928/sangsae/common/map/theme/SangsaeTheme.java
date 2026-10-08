package com.dteam0928.sangsae.common.map.theme;

public enum SangsaeTheme {
    FOOD("FOOD", "#맛집"),
    CAFE("CAFE", "#카페"),
    ACTIVITY("ACTIVITY", "#액티비티"),
    PHOTO("PHOTO", "#사진"),
    WALK("WALK", "#산책/조깅"),
    STUDY("STUDY", "#독서/공부"),
    CULTURE("CULTURE", "#문화/예술"),
    NATURE("NATURE", "#힐링/자연");

    private final String code;
    private final String displayName;

    SangsaeTheme(String code, String displayName) {
        this.code = code;
        this.displayName = displayName;
    }

    public String getCode() {
        return code;
    }

    public String getDisplayName() {
        return displayName;
    }

    // 카카오 로컬 API의 다양한 업종 분류 문자열을 상새의 8대 표준 관심사 테마(DB: interest_categories)로 매핑.
    public static SangsaeTheme fromKakao(String categoryGroupCode, String categoryName, String placeName) {
        String groupCode = categoryGroupCode != null ? categoryGroupCode.trim().toUpperCase() : "";
        String catName = categoryName != null ? categoryName : "";
        String name = placeName != null ? placeName : "";
        String combined = (catName + " " + name).toLowerCase();

        if ("CE7".equals(groupCode)) {
            return CAFE;
        }
        if ("FD6".equals(groupCode)) {
            return FOOD;
        }
        if ("CT1".equals(groupCode)) {
            return CULTURE;
        }

        if (combined.contains("카페") || combined.contains("커피") || combined.contains("디저트") || combined.contains("베이커리")) {
            return CAFE;
        }

        if (combined.contains("음식점") || combined.contains("식당") || combined.contains("맛집")) {
            return FOOD;
        }

        if (combined.contains("산책") || combined.contains("올레길") || combined.contains("둘레길") ||
            combined.contains("트레킹") || combined.contains("조깅") || combined.contains("공원") ||
            combined.contains("산책로") || combined.contains("한강공원")) {
            return WALK;
        }

        if (combined.contains("해수욕장") || combined.contains("계곡") || combined.contains("호수") ||
            combined.contains("산") || combined.contains("수목원") || combined.contains("휴양림") ||
            combined.contains("자연") || combined.contains("정원") || combined.contains("전망대")) {
            return NATURE;
        }

        if (combined.contains("헬스") || combined.contains("피트니스") || combined.contains("클라이밍") ||
            combined.contains("수영") || combined.contains("필라테스") || combined.contains("요가") ||
            combined.contains("서핑") || combined.contains("테니스") || combined.contains("배드민턴") ||
            combined.contains("스포츠") || combined.contains("레저") || combined.contains("테마파크") ||
            combined.contains("놀이공원") || combined.contains("볼링") || combined.contains("사격")) {
            return ACTIVITY;
        }

        if (combined.contains("도서관") || combined.contains("서점") || combined.contains("스터디") ||
            combined.contains("북카페") || combined.contains("독서실") || combined.contains("학원") ||
            "AC5".equals(groupCode)) {
            return STUDY;
        }

        if (combined.contains("미술관") || combined.contains("박물관") || combined.contains("전시") ||
            combined.contains("영화관") || combined.contains("공연장") || combined.contains("극장") ||
            combined.contains("문화") || combined.contains("유적지") || combined.contains("궁궐") ||
            combined.contains("사찰") || combined.contains("역사")) {
            return CULTURE;
        }

        if ("AT4".equals(groupCode) || combined.contains("포토존") || combined.contains("관광") ||
            combined.contains("명소") || combined.contains("스튜디오") || combined.contains("사진")) {
            return PHOTO;
        }

        return WALK;
    }

    // DB places.category 또는 레거시 업종 코드 문자열을 상새 표준 테마로 변환
    public static SangsaeTheme fromCodeOrName(String val) {
        if (val == null || val.isBlank()) {
            return WALK;
        }
        String clean = val.trim().toUpperCase();

        try {
            return SangsaeTheme.valueOf(clean);
        } catch (IllegalArgumentException ignored) {
        }

        if (clean.contains("GYM") || clean.contains("헬스") || clean.contains("피트니스") || clean.contains("ACTIVITY")) {
            return ACTIVITY;
        }
        if (clean.contains("PARK") || clean.contains("공원") || clean.contains("산책") || clean.contains("WALK")) {
            return WALK;
        }
        if (clean.contains("LIBRARY") || clean.contains("도서관") || clean.contains("스터디") || clean.contains("STUDY")) {
            return STUDY;
        }
        if (clean.contains("CAFE") || clean.contains("카페")) {
            return CAFE;
        }
        if (clean.contains("FOOD") || clean.contains("식당") || clean.contains("음식점")) {
            return FOOD;
        }
        if (clean.contains("관광") || clean.contains("PHOTO")) {
            return PHOTO;
        }

        return WALK;
    }
}
