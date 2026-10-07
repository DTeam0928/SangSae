package com.dteam0928.sangsae.user.exp;

// 경험치 획득량, 레벨업 요구량 및 칭호 부여 규칙을 전담하는 정책 클래스
// 향후 기획 변경, 시즌제 경험치 배율 이벤트 또는 레벨 캡 확장 시 비즈니스 서비스 수정 없이 단일 클래스에서 일괄 제어하기 위함
public class ExpPolicy {

    public static final int DEFAULT_QUEST_XP = 500;
    public static final int DEFAULT_ROUTINE_XP = 100;
    public static final int XP_PER_LEVEL = 1000;
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 100;

    private final int questDefaultXp;
    private final int routineDefaultXp;
    private final int xpPerLevel;
    private final int maxLevel;

    public ExpPolicy() {
        this(DEFAULT_QUEST_XP, DEFAULT_ROUTINE_XP, XP_PER_LEVEL, MAX_LEVEL);
    }

    public ExpPolicy(int questDefaultXp, int routineDefaultXp, int xpPerLevel, int maxLevel) {
        this.questDefaultXp = questDefaultXp;
        this.routineDefaultXp = routineDefaultXp;
        this.xpPerLevel = xpPerLevel;
        this.maxLevel = maxLevel;
    }

    // 퀘스트 완료 시 지급할 보상 경험치 결정 (개별 설정값이 양수이면 우선 적용)
    public int resolveQuestRewardXp(Integer customRewardXp) {
        if (customRewardXp != null && customRewardXp > 0) {
            return customRewardXp;
        }
        return questDefaultXp;
    }

    // 루틴 완료 시 지급할 보상 경험치 결정
    public int resolveRoutineRewardXp(Integer customRewardXp) {
        if (customRewardXp != null && customRewardXp > 0) {
            return customRewardXp;
        }
        return routineDefaultXp;
    }

    // 총 누적 경험치를 기반으로 현재 레벨, 칭호, 프로그레스바 지표 산출
    public LevelInfo calculateLevelInfo(int totalXp) {
        int safeTotalXp = Math.max(0, totalXp);

        // 1,000 XP마다 1레벨 상승하며 1~100레벨 범위로 제한
        int rawLevel = (safeTotalXp / xpPerLevel) + MIN_LEVEL;
        int level = Math.min(maxLevel, Math.max(MIN_LEVEL, rawLevel));
        boolean isMaxLevel = (level >= maxLevel);

        int currentLevelXp;
        int nextLevelRemainingXp;
        double progressPercent;

        if (isMaxLevel) {
            currentLevelXp = xpPerLevel;
            nextLevelRemainingXp = 0;
            progressPercent = 100.0;
        } else {
            currentLevelXp = safeTotalXp % xpPerLevel;
            nextLevelRemainingXp = xpPerLevel - currentLevelXp;
            progressPercent = Math.round(((double) currentLevelXp / xpPerLevel * 100.0) * 10.0) / 10.0;
        }

        String title = TitleTier.getTitleForLevel(level);

        return new LevelInfo(
                level,
                title,
                safeTotalXp,
                currentLevelXp,
                nextLevelRemainingXp,
                progressPercent,
                isMaxLevel
        );
    }

    public int getQuestDefaultXp() {
        return questDefaultXp;
    }

    public int getRoutineDefaultXp() {
        return routineDefaultXp;
    }

    public int getXpPerLevel() {
        return xpPerLevel;
    }

    public int getMaxLevel() {
        return maxLevel;
    }
}
