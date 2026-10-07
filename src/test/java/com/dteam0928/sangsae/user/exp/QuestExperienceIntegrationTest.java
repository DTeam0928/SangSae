package com.dteam0928.sangsae.user.exp;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.quest.dto.QuestCompleteResult;
import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.repository.QuestLogRepository;
import com.dteam0928.sangsae.quest.repository.QuestTemplateRepository;
import com.dteam0928.sangsae.quest.repository.UserQuestRepository;
import com.dteam0928.sangsae.quest.service.QuestService;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class QuestExperienceIntegrationTest {

    @Test
    @DisplayName("퀘스트 완료 시 기본 500 XP가 적립되고, 2회 완료 시(1,000 XP) 2레벨로 레벨업된다")
    void completeQuest_experienceRewardAndLevelUp() throws Exception {
        // 인메모리 데이터 저장소
        Map<Long, UserProfile> profileDb = new HashMap<>();
        Map<Long, UserQuest> userQuestDb = new HashMap<>();
        Map<Long, QuestLog> questLogDb = new HashMap<>();

        Long userId = 100L;
        UserProfile initialProfile = new UserProfile(userId, "탐험가길동");
        initialProfile.setTotalXp(0);
        profileDb.put(userId, initialProfile);

        // 첫 번째 퀘스트 (보상 XP 500)
        UserQuest quest1 = new UserQuest();
        quest1.setId(1L);
        quest1.setUserId(userId);
        quest1.setTitle("동네 공원 산책");
        quest1.setScheduledDate(LocalDate.now());
        quest1.setAuthType("NONE");
        quest1.setStatus("TODO");
        quest1.setRewardXp(500);
        userQuestDb.put(1L, quest1);

        // 두 번째 퀘스트 (보상 XP 0 -> ExpPolicy 기본 500 적용)
        UserQuest quest2 = new UserQuest();
        quest2.setId(2L);
        quest2.setUserId(userId);
        quest2.setTitle("카페 방문 인증");
        quest2.setScheduledDate(LocalDate.now());
        quest2.setAuthType("NONE");
        quest2.setStatus("TODO");
        quest2.setRewardXp(0);
        userQuestDb.put(2L, quest2);

        // Mock DAO & TransactionTemplate
        UserProfileDao profileDao = new UserProfileDao() {
            @Override public void insert(EntityManager em, UserProfile profile) { profileDb.put(profile.getUserId(), profile); }
            @Override public Optional<UserProfile> findByUserId(EntityManager em, long uid) { return Optional.ofNullable(profileDb.get(uid)); }
            @Override public boolean existsByNickname(EntityManager em, String nickname) { return false; }
            @Override public void updateNickname(EntityManager em, long uid, String nickname) {}
            @Override public void updateLocation(EntityManager em, long uid, boolean permitted, com.dteam0928.sangsae.common.geo.GeoPoint location) {}
        };

        TransactionTemplate fakeTx = new TransactionTemplate(null) {
            @Override
            public <T> T inTransaction(com.dteam0928.sangsae.common.db.EmFunction<T> action) {
                return action.apply(null);
            }
            @Override
            public <T> T read(com.dteam0928.sangsae.common.db.EmFunction<T> action) {
                return action.apply(null);
            }
        };

        ExpPolicy expPolicy = new ExpPolicy();
        ExperienceService experienceService = new ExperienceService(fakeTx, profileDao, expPolicy) {
            @Override
            public ExpRewardResult awardQuestExp(EntityManager em, Long uid, Integer customRewardXp) {
                UserProfile p = profileDb.get(uid);
                int previousXp = p.getTotalXp();
                int earned = expPolicy.resolveQuestRewardXp(customRewardXp);
                int newXp = previousXp + earned;
                p.setTotalXp(newXp);

                LevelInfo prevLvl = expPolicy.calculateLevelInfo(previousXp);
                LevelInfo currLvl = expPolicy.calculateLevelInfo(newXp);
                return ExpRewardResult.of(earned, previousXp, newXp, prevLvl, currLvl);
            }
        };

        QuestService questService = new QuestService(fakeTx, null, null, experienceService);

        // Repository 가짜 바인딩
        setField(questService, "userQuestRepository", new UserQuestRepository() {
            @Override public UserQuest findById(EntityManager em, Long id) { return userQuestDb.get(id); }
            @Override public void delete(EntityManager em, UserQuest userQuest) {
                if (userQuest != null) {
                    userQuestDb.remove(userQuest.getId());
                }
            }
        });
        setField(questService, "questLogRepository", new QuestLogRepository() {
            @Override public QuestLog save(EntityManager em, QuestLog log) {
                log.setId(10L + questLogDb.size());
                questLogDb.put(log.getId(), log);
                return log;
            }
        });
        setField(questService, "questRepository", new QuestTemplateRepository() {
            @Override public QuestTemplate findById(EntityManager em, Long id) { return null; }
        });

        // 1. 첫 번째 퀘스트 완료 수행 (500 XP)
        QuestCompleteResult result1 = questService.completeQuestWithReward(1L, userId, null, null, null, null);

        assertNotNull(result1);
        assertEquals(500, result1.questLog().getEarnedXp());
        assertEquals("COMPLETED", quest1.getStatus());
        assertNull(userQuestDb.get(1L), "완료된 퀘스트는 일정(userQuestDb)에서 자동 삭제되어야 함");
        assertEquals(500, profileDb.get(userId).getTotalXp());

        ExpRewardResult reward1 = result1.expRewardResult();
        assertNotNull(reward1);
        assertEquals(500, reward1.earnedXp());
        assertEquals(1, reward1.currentLevel().level());
        assertEquals(50.0, reward1.currentLevel().progressPercent());
        assertFalse(reward1.isLevelUp(), "첫 500 XP에서는 아직 레벨 1이어야 함");

        // 2. 두 번째 퀘스트 완료 수행 (500 XP 추가 -> 총 1,000 XP)
        QuestCompleteResult result2 = questService.completeQuestWithReward(2L, userId, null, null, null, null);

        assertNotNull(result2);
        assertEquals(500, result2.questLog().getEarnedXp());
        assertEquals("COMPLETED", quest2.getStatus());
        assertNull(userQuestDb.get(2L), "완료된 퀘스트는 일정(userQuestDb)에서 자동 삭제되어야 함");
        assertEquals(1000, profileDb.get(userId).getTotalXp());

        ExpRewardResult reward2 = result2.expRewardResult();
        assertNotNull(reward2);
        assertEquals(500, reward2.earnedXp());
        assertEquals(2, reward2.currentLevel().level());
        assertEquals(0.0, reward2.currentLevel().progressPercent());
        assertTrue(reward2.isLevelUp(), "총 1,000 XP에 도달하여 2레벨로 레벨업해야 함");
    }

    @Test
    @DisplayName("사용자 미완료 퀘스트를 일정에서 정상 삭제할 수 있고, 타인 또는 완료 퀘스트는 삭제가 차단된다")
    void deleteUserQuest_validation() throws Exception {
        Long ownerId = 1L;
        Long otherId = 2L;

        Map<Long, UserQuest> userQuestDb = new HashMap<>();

        UserQuest todoQuest = new UserQuest();
        todoQuest.setId(10L);
        todoQuest.setUserId(ownerId);
        todoQuest.setStatus("TODO");
        userQuestDb.put(10L, todoQuest);

        UserQuest completedQuest = new UserQuest();
        completedQuest.setId(20L);
        completedQuest.setUserId(ownerId);
        completedQuest.setStatus("COMPLETED");
        userQuestDb.put(20L, completedQuest);

        TransactionTemplate fakeTx = new TransactionTemplate(null) {
            @Override
            public <T> T inTransaction(com.dteam0928.sangsae.common.db.EmFunction<T> action) {
                return action.apply(null);
            }
        };

        QuestService questService = new QuestService(fakeTx);
        setField(questService, "userQuestRepository", new UserQuestRepository() {
            @Override public UserQuest findById(EntityManager em, Long id) { return userQuestDb.get(id); }
            @Override public void delete(EntityManager em, UserQuest userQuest) {
                if (userQuest != null) {
                    userQuestDb.remove(userQuest.getId());
                }
            }
        });

        // 1. 정상 삭제 성공
        boolean deleted = questService.deleteUserQuest(10L, ownerId);
        assertTrue(deleted);
        assertNull(userQuestDb.get(10L), "삭제 후 DB에서 제거되어야 함");

        // 2. 타인 퀘스트 삭제 시도 차단
        assertThrows(IllegalArgumentException.class, () -> questService.deleteUserQuest(20L, otherId));

        // 3. 완료된 퀘스트 직접 삭제 시도 차단
        assertThrows(IllegalStateException.class, () -> questService.deleteUserQuest(20L, ownerId));
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = QuestService.class.getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
