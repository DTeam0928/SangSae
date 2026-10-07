package com.dteam0928.sangsae.user.exp;

// 경험치 지급 결과 및 레벨업 상태 전이 DTO
// 퀘스트/루틴 완료 시 클라이언트가 레벨업 및 칭호 획득 축하 모달을 띄울 수 있도록 상태 변화를 제공
public record ExpRewardResult(
        int earnedXp,
        int previousTotalXp,
        int newTotalXp,
        LevelInfo previousLevel,
        LevelInfo currentLevel,
        boolean isLevelUp,
        boolean isTitleChanged
) {
    public static ExpRewardResult of(
            int earnedXp,
            int previousTotalXp,
            int newTotalXp,
            LevelInfo previousLevel,
            LevelInfo currentLevel) {
        boolean levelUp = currentLevel.level() > previousLevel.level();
        boolean titleChanged = !currentLevel.title().equals(previousLevel.title());
        return new ExpRewardResult(
                earnedXp,
                previousTotalXp,
                newTotalXp,
                previousLevel,
                currentLevel,
                levelUp,
                titleChanged
        );
    }
}
