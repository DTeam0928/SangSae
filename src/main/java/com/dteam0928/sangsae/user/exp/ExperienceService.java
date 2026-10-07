package com.dteam0928.sangsae.user.exp;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;

import java.util.Objects;

// 회원 경험치 적립, 레벨 산출 및 칭호 부여를 전담하는 비즈니스 서비스
// 활동 도메인(퀘스트, 루틴)과의 결합도를 낮추고 일관된 보상 트랜잭션을 보장하기 위해 분리
public class ExperienceService {

    private final TransactionTemplate tx;
    private final UserProfileDao userProfileDao;
    private final ExpPolicy expPolicy;

    public ExperienceService(TransactionTemplate tx, UserProfileDao userProfileDao, ExpPolicy expPolicy) {
        this.tx = Objects.requireNonNull(tx, "TransactionTemplate은 필수입니다.");
        this.userProfileDao = Objects.requireNonNull(userProfileDao, "UserProfileDao는 필수입니다.");
        this.expPolicy = Objects.requireNonNull(expPolicy, "ExpPolicy는 필수입니다.");
    }

    // 회원 현재 레벨, 칭호 및 경험치 진행 정보 조회
    public LevelInfo getUserLevelInfo(Long userId) {
        if (userId == null) {
            return expPolicy.calculateLevelInfo(0);
        }
        return tx.read(em -> {
            UserProfile profile = em.find(UserProfile.class, userId);
            int totalXp = (profile != null) ? profile.getTotalXp() : 0;
            return expPolicy.calculateLevelInfo(totalXp);
        });
    }

    // 기존 퀘스트 완료 트랜잭션(EntityManager)에 참여하여 경험치 적립 및 레벨업 판정 수행
    public ExpRewardResult awardQuestExp(EntityManager em, Long userId, Integer customRewardXp) {
        int earnedXp = expPolicy.resolveQuestRewardXp(customRewardXp);
        return applyExperience(em, userId, earnedXp);
    }

    // 기존 루틴 완료 트랜잭션(EntityManager)에 참여하여 경험치 적립 및 레벨업 판정 수행 (확장성 보장)
    public ExpRewardResult awardRoutineExp(EntityManager em, Long userId, Integer customRewardXp) {
        int earnedXp = expPolicy.resolveRoutineRewardXp(customRewardXp);
        return applyExperience(em, userId, earnedXp);
    }

    // 독립 트랜잭션 단위 퀘스트 경험치 적립
    public ExpRewardResult awardQuestExp(Long userId, Integer customRewardXp) {
        return tx.inTransaction(em -> awardQuestExp(em, userId, customRewardXp));
    }

    // 독립 트랜잭션 단위 루틴 경험치 적립
    public ExpRewardResult awardRoutineExp(Long userId, Integer customRewardXp) {
        return tx.inTransaction(em -> awardRoutineExp(em, userId, customRewardXp));
    }

    public ExpPolicy getExpPolicy() {
        return expPolicy;
    }

    private ExpRewardResult applyExperience(EntityManager em, Long userId, int earnedXp) {
        UserProfile profile = em.find(UserProfile.class, userId);
        if (profile == null) {
            // 회원 프로필이 아직 생성되지 않은 비정상 상태 방어
            LevelInfo defaultLevel = expPolicy.calculateLevelInfo(0);
            return ExpRewardResult.of(0, 0, 0, defaultLevel, defaultLevel);
        }

        int previousTotalXp = profile.getTotalXp();
        int newTotalXp = previousTotalXp + earnedXp;

        // Dirty checking을 통해 user_profiles.total_xp 자동 갱신
        profile.setTotalXp(newTotalXp);

        LevelInfo previousLevel = expPolicy.calculateLevelInfo(previousTotalXp);
        LevelInfo currentLevel = expPolicy.calculateLevelInfo(newTotalXp);

        return ExpRewardResult.of(earnedXp, previousTotalXp, newTotalXp, previousLevel, currentLevel);
    }
}
