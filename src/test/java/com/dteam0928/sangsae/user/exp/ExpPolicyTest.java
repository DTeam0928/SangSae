package com.dteam0928.sangsae.user.exp;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExpPolicyTest {

    private final ExpPolicy policy = new ExpPolicy();

    @Test
    @DisplayName("0 XP 보유 시 1레벨, 새싹 탐험가 칭호, 진행률 0%를 산출한다")
    void calculateLevelInfo_initial() {
        LevelInfo info = policy.calculateLevelInfo(0);

        assertEquals(1, info.level());
        assertEquals("새싹 탐험가", info.title());
        assertEquals(0, info.totalXp());
        assertEquals(0, info.currentLevelXp());
        assertEquals(1000, info.nextLevelRemainingXp());
        assertEquals(0.0, info.progressPercent());
        assertFalse(info.isMaxLevel());
    }

    @Test
    @DisplayName("500 XP 보유 시 1레벨, 진행률 50%, 다음 레벨까지 500 XP 남음을 산출한다")
    void calculateLevelInfo_halfway() {
        LevelInfo info = policy.calculateLevelInfo(500);

        assertEquals(1, info.level());
        assertEquals("새싹 탐험가", info.title());
        assertEquals(500, info.totalXp());
        assertEquals(500, info.currentLevelXp());
        assertEquals(500, info.nextLevelRemainingXp());
        assertEquals(50.0, info.progressPercent());
        assertFalse(info.isMaxLevel());
    }

    @Test
    @DisplayName("1,000 XP 도달 시 정확히 2레벨로 레벨업하고 현재 레벨 잔여 경험치는 0 XP이다")
    void calculateLevelInfo_exactLevelUp() {
        LevelInfo info = policy.calculateLevelInfo(1000);

        assertEquals(2, info.level());
        assertEquals("새싹 탐험가", info.title());
        assertEquals(1000, info.totalXp());
        assertEquals(0, info.currentLevelXp());
        assertEquals(1000, info.nextLevelRemainingXp());
        assertEquals(0.0, info.progressPercent());
    }

    @Test
    @DisplayName("10레벨 단위 구간마다 정의된 칭호가 정확하게 변경된다")
    void calculateLevelInfo_titleTiers() {
        assertEquals("새싹 탐험가", policy.calculateLevelInfo(0).title());       // Lv.1
        assertEquals("새싹 탐험가", policy.calculateLevelInfo(9000).title());    // Lv.10
        assertEquals("골목의 산책자", policy.calculateLevelInfo(10000).title());  // Lv.11
        assertEquals("일상의 수집가", policy.calculateLevelInfo(20000).title());  // Lv.21
        assertEquals("동네의 나침반", policy.calculateLevelInfo(30000).title());  // Lv.31
        assertEquals("도시의 모험가", policy.calculateLevelInfo(40000).title());  // Lv.41
        assertEquals("취향의 개척자", policy.calculateLevelInfo(50000).title());  // Lv.51
        assertEquals("풍경의 기록자", policy.calculateLevelInfo(60000).title());  // Lv.61
        assertEquals("지평선의 항해자", policy.calculateLevelInfo(70000).title()); // Lv.71
        assertEquals("여정의 마스터", policy.calculateLevelInfo(80000).title());   // Lv.81
        assertEquals("상새의 전설", policy.calculateLevelInfo(90000).title());   // Lv.91
    }

    @Test
    @DisplayName("99,000 XP 이상 획득 시 100레벨 만렙으로 제한되고 진행률은 100%이다")
    void calculateLevelInfo_maxLevelCap() {
        LevelInfo info = policy.calculateLevelInfo(99000);

        assertEquals(100, info.level());
        assertEquals("상새의 전설", info.title());
        assertTrue(info.isMaxLevel());
        assertEquals(1000, info.currentLevelXp());
        assertEquals(0, info.nextLevelRemainingXp());
        assertEquals(100.0, info.progressPercent());

        // 초과 경험치 획득 시에도 100레벨 초과 금지
        LevelInfo overflowInfo = policy.calculateLevelInfo(200000);
        assertEquals(100, overflowInfo.level());
        assertTrue(overflowInfo.isMaxLevel());
    }

    @Test
    @DisplayName("음수 경험치 입력 시 0으로 안전하게 클램핑된다")
    void calculateLevelInfo_negativeClamping() {
        LevelInfo info = policy.calculateLevelInfo(-500);

        assertEquals(1, info.level());
        assertEquals(0, info.totalXp());
    }

    @Test
    @DisplayName("퀘스트 및 루틴 경험치 보상 계산 시 커스텀 값이 없으면 기본값을 반환한다")
    void resolveRewards() {
        // 퀘스트 기본 500 XP
        assertEquals(500, policy.resolveQuestRewardXp(null));
        assertEquals(500, policy.resolveQuestRewardXp(0));
        assertEquals(300, policy.resolveQuestRewardXp(300));

        // 루틴 기본 100 XP
        assertEquals(100, policy.resolveRoutineRewardXp(null));
        assertEquals(100, policy.resolveRoutineRewardXp(-10));
        assertEquals(150, policy.resolveRoutineRewardXp(150));
    }
}
