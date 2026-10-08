package com.dteam0928.sangsae.home;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.repository.QuestTemplateRepository;
import com.dteam0928.sangsae.quest.repository.UserQuestRepository;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskResponseDto;
import com.dteam0928.sangsae.routine.service.RoutineService;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
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

        LevelInfo levelInfo = (experienceService != null && userId != null)
                ? experienceService.getUserLevelInfo(userId)
                : new LevelInfo(1, "새싹 탐험가", totalXp, totalXp, 1000 - totalXp, 0.0, false);

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
        aggregate.setCurrentStreak(Math.max(1, (routineSuccess + questDoneCount)));

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
}
