package com.dteam0928.sangsae.home;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.repository.QuestTemplateRepository;
import com.dteam0928.sangsae.quest.repository.UserQuestRepository;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskResponseDto;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.exp.ExpPolicy;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import com.dteam0928.sangsae.user.exp.LevelInfo;
import com.dteam0928.sangsae.user.model.UserProfile;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * H-00 홈 화면 대시보드 집계 서비스.
 * 사용자별 경험치, 오늘 퀘스트/루틴 진행 통계, 추천 퀘스트를 통합 수집.
 */
public class HomeDashboardService {

    private final TransactionTemplate tx;
    private final UserProfileDao profileDao;
    private final ExperienceService experienceService;
    private final RoutineService routineService;
    private final UserQuestRepository userQuestRepository;
    private final QuestTemplateRepository questTemplateRepository;

    public HomeDashboardService(TransactionTemplate tx,
                                UserProfileDao profileDao,
                                ExperienceService experienceService,
                                RoutineService routineService,
                                UserQuestRepository userQuestRepository,
                                QuestTemplateRepository questTemplateRepository) {
        this.tx = tx;
        this.profileDao = profileDao;
        this.experienceService = experienceService;
        this.routineService = routineService;
        this.userQuestRepository = userQuestRepository;
        this.questTemplateRepository = questTemplateRepository;
    }

    public HomeModel buildDashboard(Long userId, String sessionNickname) {
        HomeModel model = new HomeModel();
        LocalDate today = LocalDate.now();

        // 1. 프로필 및 경험치 정보
        String nickname = sessionNickname != null ? sessionNickname : "탐험가";
        int totalXp = 0;
        if (userId != null) {
            Optional<UserProfile> profileOpt = tx.read(em -> profileDao.findByUserId(em, userId));
            if (profileOpt.isPresent()) {
                UserProfile profile = profileOpt.get();
                if (profile.getNickname() != null && !profile.getNickname().isBlank()) {
                    nickname = profile.getNickname();
                }
                totalXp = profile.getTotalXp();
            }
        }
        model.setNickname(nickname);

        ExpPolicy policy = (experienceService != null)
                ? experienceService.getExpPolicy()
                : new ExpPolicy();

        LevelInfo levelInfo = (experienceService != null && userId != null)
                ? experienceService.getUserLevelInfo(userId)
                : policy.calculateLevelInfo(totalXp);

        model.setLevelInfo(levelInfo);
        model.getUi().setXpTitle("Lv." + levelInfo.level() + " " + levelInfo.title());

        // 2. 오늘의 퀘스트 현황 수집
        int questDoneCount = 0;
        int questRemainingCount = 0;
        if (userId != null) {
            List<UserQuest> userQuests = tx.read(em -> userQuestRepository.findByUserIdAndTripIdAndScheduledDate(em, userId, null, today));
            for (UserQuest uq : userQuests) {
                boolean isCompleted = "COMPLETED".equals(uq.getStatus());
                if (isCompleted) {
                    questDoneCount++;
                } else {
                    questRemainingCount++;
                }
                model.getTrips().add(new HomeModel.ManagementItem(
                        uq.getId(),
                        uq.getTitle(),
                        today.toString(),
                        today.toString(),
                        isCompleted ? "completed" : "active"
                ));
            }
        }

        // 오늘 완료한 퀘스트 수는 quest_logs에서 조회
        if (userId != null) {
            Number completedCount = tx.read(em ->
                (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM quest_logs " +
                    "WHERE user_id = ?1 " +
                    "AND completed_at >= ?2 " +
                    "AND completed_at < ?3"
                )
                .setParameter(1, userId)
                .setParameter(2, today.atStartOfDay())
                .setParameter(3, today.plusDays(1).atStartOfDay())
                .getSingleResult()
            );

            questDoneCount = completedCount.intValue();
        }
        
        // 오늘 완료한 퀘스트 목록 조회
        if (userId != null) {
            List<?> completedQuests = tx.read(em ->
                em.createNativeQuery(
                    "SELECT id, title, completed_at " +
                    "FROM quest_logs " +
                    "WHERE user_id = ?1 " +
                    "AND completed_at >= ?2 " +
                    "AND completed_at < ?3 " +
                    "ORDER BY completed_at DESC"
                )
                .setParameter(1, userId)
                .setParameter(2, today.atStartOfDay())
                .setParameter(3, today.plusDays(1).atStartOfDay())
                .getResultList()
            );

            for (Object result : completedQuests) {
                Object[] row = (Object[]) result;

                Long logId = ((Number) row[0]).longValue();
                String title = row[1] != null
                        ? row[1].toString()
                        : "완료한 퀘스트";
                String completedAt = row[2].toString();

                model.getTrips().add(new HomeModel.ManagementItem(
                    logId,
                    title,
                    completedAt,
                    today.toString(),
                    "completed"
                ));
            }
        }
        
        // 3. 오늘의 루틴 현황 수집
        int routineTotal = 0;
        int routineSuccess = 0;
        if (userId != null && routineService != null) {
            List<UserRoutineTaskResponseDto> timetable = routineService.getTodayTimeline(userId, today);
            routineTotal = timetable.size();
            for (UserRoutineTaskResponseDto task : timetable) {
                if (task.isDoneToday()) {
                    routineSuccess++;
                }
                model.getRoutines().add(new HomeModel.ManagementItem(
                        task.id(),
                        task.routineTitle() + " : " + task.content(),
                        task.targetTime() != null ? task.targetTime().toString() : "매일",
                        today.toString(),
                        task.isDoneToday() ? "completed" : "active"
                ));
            }
        }
        int routineRemaining = Math.max(0, routineTotal - routineSuccess);

        // 4. 일일 집계 지표 세팅
        HomeModel.DailyAggregateDto aggregate = model.getDailyAggregate();
        aggregate.setRoutineTotalCount(routineTotal);
        aggregate.setRoutineSuccessCount(routineSuccess);
        aggregate.setQuestSuccessCount(questDoneCount);
        aggregate.setEarnedXp(totalXp);
                aggregate.setCurrentStreak(userId == null ? 0 : calculateStreak(userId, today));

        // 5. 통계 맵 바인딩 (statsCards)
        model.getStats().put("questDone", questDoneCount);
        model.getStats().put("questRemaining", questRemainingCount);
        model.getStats().put("routineDone", routineSuccess);
        model.getStats().put("routineRemaining", routineRemaining);
        model.getStats().put("totalXp", totalXp);
        model.getStats().put("level", levelInfo.level());
        model.getStats().put("title", levelInfo.title());

        // 6. 추천 퀘스트 1건 선정
        try {
            List<QuestTemplate> publicQuests = tx.read(em -> questTemplateRepository.findAll(em));
            if (!publicQuests.isEmpty()) {
                QuestTemplate rec = publicQuests.get(0);
                model.setRecommendedQuest(new HomeModel.FeatureItem(
                        rec.getTitle(),
                        rec.getCategory(),
                        rec.getDescription() != null ? rec.getDescription() : "새로운 장소에서 일상을 시작해보세요."
                ));
            }
        } catch (Exception ignored) {
        }

        return model;
    }

    
    /**
     * 연속 실천일: 루틴 Task 또는 퀘스트를 하루 1개 이상 완료한 날이
     * 오늘(오늘 기록이 아직 없으면 어제)부터 끊기지 않고 이어진 일수.
     * 같은 날 여러 개를 완료해도 하루로 센다.
     */
    private int calculateStreak(Long userId, LocalDate today) {
        List<?> dates = tx.read(em -> em.createNativeQuery(
                        "SELECT d FROM ("
                                + " SELECT l.log_date AS d FROM routine_task_logs l"
                                + " WHERE l.user_id = ?1 AND l.is_success = 1 AND l.log_date <= ?2"
                                + " UNION"   // UNION 이 같은 날짜를 하나로 합친다
                                + " SELECT DATE(q.completed_at) FROM quest_logs q"
                                + " WHERE q.user_id = ?3 AND DATE(q.completed_at) <= ?4"
                                + ") x ORDER BY d DESC")
                .setParameter(1, userId)
                .setParameter(2, today)
                .setParameter(3, userId)
                .setParameter(4, today)
                .setMaxResults(400)
                .getResultList());

        LocalDate expected = null;
        int count = 0;
        for (Object row : dates) {
            LocalDate d = toLocalDate(row);
            if (expected == null) {
                if (d.equals(today)) expected = today;
                else if (d.equals(today.minusDays(1))) expected = today.minusDays(1);
                else return 0;   // 어제도 기록이 없으면 연속 끊김
            }
            if (!d.equals(expected)) break;
            count++;
            expected = expected.minusDays(1);
        }
        return count;
    }

    /** DATE 컬럼 값이 드라이버에 따라 java.sql.Date 또는 LocalDate 로 온다 */
    private static LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate d) return d;
        if (value instanceof java.sql.Date d) return d.toLocalDate();
        return LocalDate.parse(value.toString().substring(0, 10));
    }
}
