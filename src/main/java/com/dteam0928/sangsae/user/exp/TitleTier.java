package com.dteam0928.sangsae.user.exp;

import java.util.Arrays;

// 10레벨 단위 칭호 티어 열거형
// 기획 변경 시 칭호 명칭이나 구간 조정을 비즈니스 로직과 분리하여 손쉽게 관리하기 위함
public enum TitleTier {
    TIER_1(1, 10, "새싹 탐험가"),
    TIER_2(11, 20, "골목의 산책자"),
    TIER_3(21, 30, "일상의 수집가"),
    TIER_4(31, 40, "동네의 나침반"),
    TIER_5(41, 50, "도시의 모험가"),
    TIER_6(51, 60, "취향의 개척자"),
    TIER_7(61, 70, "풍경의 기록자"),
    TIER_8(71, 80, "지평선의 항해자"),
    TIER_9(81, 90, "여정의 마스터"),
    TIER_10(91, 100, "상새의 전설");

    private final int minLevel;
    private final int maxLevel;
    private final String titleName;

    TitleTier(int minLevel, int maxLevel, String titleName) {
        this.minLevel = minLevel;
        this.maxLevel = maxLevel;
        this.titleName = titleName;
    }

    public int getMinLevel() {
        return minLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public String getTitleName() {
        return titleName;
    }

    // 주어진 레벨에 해당하는 칭호명 반환 (1 미만은 1티어, 100 초과는 최고 티어 매핑)
    public static String getTitleForLevel(int level) {
        if (level <= 1) {
            return TIER_1.titleName;
        }
        if (level >= 100) {
            return TIER_10.titleName;
        }
        return Arrays.stream(values())
                .filter(tier -> level >= tier.minLevel && level <= tier.maxLevel)
                .findFirst()
                .map(TitleTier::getTitleName)
                .orElse(TIER_1.titleName);
    }
}
