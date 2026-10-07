package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.routine.dto.RoutineTaskCompleteResult;
import com.dteam0928.sangsae.routine.dto.RoutineTemplateDetailDto;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskCreateRequest;
import com.dteam0928.sangsae.routine.dto.UserRoutineTaskResponseDto;
import com.dteam0928.sangsae.routine.entity.RoutineTaskLog;
import com.dteam0928.sangsae.routine.entity.RoutineTaskSchedule;
import com.dteam0928.sangsae.routine.entity.RoutineTemplate;
import com.dteam0928.sangsae.routine.entity.RoutineTemplateTask;
import com.dteam0928.sangsae.routine.entity.UserRoutine;
import com.dteam0928.sangsae.routine.entity.UserRoutineTask;
import com.dteam0928.sangsae.routine.repository.RoutineTaskLogRepository;
import com.dteam0928.sangsae.routine.repository.RoutineTemplateRepository;
import com.dteam0928.sangsae.routine.repository.UserRoutineRepository;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.exp.ExpPolicy;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Proxy;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RoutineServiceIntegrationTest {

    private final Map<Long, RoutineTemplate> templateDb = new HashMap<>();
    private final Map<Long, List<RoutineTemplateTask>> templateTasksDb = new HashMap<>();
    private final Map<Long, UserRoutine> userRoutineDb = new HashMap<>();
    private final Map<Long, UserRoutineTask> userTaskDb = new HashMap<>();
    private final Map<Long, List<RoutineTaskSchedule>> scheduleDb = new HashMap<>();
    private final List<RoutineTaskLog> logDb = new ArrayList<>();
    private final Map<Long, UserProfile> profileDb = new HashMap<>();
    private final Map<Long, PlaceResponseDto> placeDb = new HashMap<>();

    private RoutineService routineService;
    private ExperienceService experienceService;
    private Long idSequence = 1L;

    @BeforeEach
    void setUp() {
        templateDb.clear();
        templateTasksDb.clear();
        userRoutineDb.clear();
        userTaskDb.clear();
        scheduleDb.clear();
        logDb.clear();
        profileDb.clear();
        placeDb.clear();

        // 1. TransactionTemplate Mock
        EntityManager mockEm = (EntityManager) Proxy.newProxyInstance(
            EntityManager.class.getClassLoader(),
            new Class<?>[]{EntityManager.class},
            (proxy, method, args) -> {
                if ("find".equals(method.getName()) && args != null && args.length >= 2) {
                    if (args[0] == UserProfile.class) {
                        return profileDb.get(args[1]);
                    }
                }
                return null;
            }
        );

        TransactionTemplate stubTx = new TransactionTemplate(null) {
            @Override
            public <T> T inTransaction(com.dteam0928.sangsae.common.db.EmFunction<T> work) {
                return work.apply(mockEm);
            }
            @Override
            public <T> T read(com.dteam0928.sangsae.common.db.EmFunction<T> work) {
                return work.apply(mockEm);
            }
        };

        // 2. Mock Repositories
        RoutineTemplateRepository templateRepo = new RoutineTemplateRepository() {
            @Override
            public RoutineTemplate findById(EntityManager em, Long id) {
                return templateDb.get(id);
            }
            @Override
            public List<RoutineTemplate> findPublicTemplates(EntityManager em, String category) {
                return new ArrayList<>(templateDb.values());
            }
            @Override
            public List<RoutineTemplateTask> findTasksByTemplateId(EntityManager em, Long routineTemplateId) {
                return templateTasksDb.getOrDefault(routineTemplateId, new ArrayList<>());
            }
            @Override
            public void incrementAdoptedCount(EntityManager em, Long templateId) {
                RoutineTemplate t = templateDb.get(templateId);
                if (t != null) t.setAdoptedCount(t.getAdoptedCount() + 1);
            }
        };

        UserRoutineRepository userRoutineRepo = new UserRoutineRepository() {
            @Override
            public UserRoutine saveRoutine(EntityManager em, UserRoutine userRoutine) {
                if (userRoutine.getId() == null) userRoutine.setId(idSequence++);
                userRoutineDb.put(userRoutine.getId(), userRoutine);
                return userRoutine;
            }
            @Override
            public UserRoutineTask saveTask(EntityManager em, UserRoutineTask task) {
                if (task.getId() == null) task.setId(idSequence++);
                userTaskDb.put(task.getId(), task);
                return task;
            }
            @Override
            public void saveSchedules(EntityManager em, List<RoutineTaskSchedule> schedules) {
                if (schedules == null || schedules.isEmpty()) return;
                Long taskId = schedules.get(0).getUserRoutineTaskId();
                scheduleDb.computeIfAbsent(taskId, k -> new ArrayList<>()).addAll(schedules);
            }
            @Override
            public UserRoutine findRoutineById(EntityManager em, Long id) {
                return userRoutineDb.get(id);
            }
            @Override
            public UserRoutineTask findTaskById(EntityManager em, Long id) {
                return userTaskDb.get(id);
            }
            @Override
            public List<UserRoutine> findActiveRoutinesByUserId(EntityManager em, Long userId) {
                return userRoutineDb.values().stream().filter(r -> r.getUserId().equals(userId) && r.isActive()).toList();
            }
            @Override
            public List<UserRoutineTask> findTasksByUserRoutineIds(EntityManager em, List<Long> routineIds) {
                return userTaskDb.values().stream().filter(t -> routineIds.contains(t.getUserRoutineId()) && t.isActive()).toList();
            }
            @Override
            public List<RoutineTaskSchedule> findSchedulesByTaskIds(EntityManager em, List<Long> taskIds) {
                List<RoutineTaskSchedule> res = new ArrayList<>();
                for (Long tid : taskIds) {
                    if (scheduleDb.containsKey(tid)) res.addAll(scheduleDb.get(tid));
                }
                return res;
            }
        };

        RoutineTaskLogRepository logRepo = new RoutineTaskLogRepository() {
            @Override
            public RoutineTaskLog save(EntityManager em, RoutineTaskLog log) {
                if (log.getId() == null) log.setId(idSequence++);
                logDb.add(log);
                return log;
            }
            @Override
            public RoutineTaskLog findTodayLog(EntityManager em, Long userId, Long userRoutineTaskId, LocalDate date) {
                return logDb.stream().filter(l -> l.getUserId().equals(userId) && l.getUserRoutineTaskId().equals(userRoutineTaskId) && l.getLogDate().equals(date)).findFirst().orElse(null);
            }
            @Override
            public List<RoutineTaskLog> findTodayLogsByUser(EntityManager em, Long userId, LocalDate date) {
                return logDb.stream().filter(l -> l.getUserId().equals(userId) && l.getLogDate().equals(date)).toList();
            }
        };

        // 3. UserProfileDao Mock
        UserProfileDao profileDao = new UserProfileDao() {
            @Override public void insert(EntityManager em, UserProfile profile) { profileDb.put(profile.getUserId(), profile); }
            @Override public Optional<UserProfile> findByUserId(EntityManager em, long userId) { return Optional.ofNullable(profileDb.get(userId)); }
            @Override public boolean existsByNickname(EntityManager em, String nickname) { return false; }
            @Override public void updateNickname(EntityManager em, long uid, String nickname) {}
            @Override public void updateLocation(EntityManager em, long uid, boolean permitted, com.dteam0928.sangsae.common.geo.GeoPoint location) {}
        };

        // 4. ExperienceService & LocationAuthService
        ExpPolicy policy = new ExpPolicy(500, 100, 1000, 100);
        experienceService = new ExperienceService(stubTx, profileDao, policy);

        PlaceService placeService = new PlaceService(null) {
            @Override
            public PlaceResponseDto getPlaceById(Long id) {
                return placeDb.get(id);
            }
        };

        LocationAuthService locationAuthService = new LocationAuthService(placeService);

        routineService = new RoutineService(stubTx, placeService, locationAuthService, experienceService,
                templateRepo, userRoutineRepo, logRepo);
    }

    @Test
    @DisplayName("루틴 템플릿 담기(Fork) 시 하위 Task N개가 일괄 복제되고 adopted_count가 증가한다")
    void adoptRoutine_forkPipeline() {
        Long templateId = 10L;
        RoutineTemplate tpl = new RoutineTemplate(1L, null, "미라클 모닝 3종", "아침 습관 형성", "HEALTH", "PUBLIC");
        tpl.setId(templateId);
        templateDb.put(templateId, tpl);

        List<RoutineTemplateTask> tplTasks = new ArrayList<>();
        tplTasks.add(new RoutineTemplateTask(templateId, null, "기상 후 물 500ml", LocalTime.of(7, 0), "HEALTH", 100, 1, 50));
        tplTasks.add(new RoutineTemplateTask(templateId, null, "스트레칭 10분", LocalTime.of(7, 15), "HEALTH", 100, 2, 50));
        tplTasks.add(new RoutineTemplateTask(templateId, null, "독서 20분", LocalTime.of(7, 30), "STUDY", 100, 3, 50));
        templateTasksDb.put(templateId, tplTasks);

        Long userId = 999L;
        UserRoutine adopted = routineService.adoptRoutine(userId, templateId, LocalDate.now(), null);

        assertNotNull(adopted.getId());
        assertEquals("미라클 모닝 3종", adopted.getTitle());
        assertEquals(templateId, adopted.getOriginTemplateId());
        assertEquals(1, tpl.getAdoptedCount());

        List<UserRoutineTask> userTasks = userTaskDb.values().stream()
                .filter(t -> t.getUserRoutineId().equals(adopted.getId())).toList();
        assertEquals(3, userTasks.size());
        assertEquals("기상 후 물 500ml", userTasks.get(0).getContent());
    }

    @Test
    @DisplayName("나만의 루틴 직접 생성 및 커스텀 Task 추가 (가변 인증 반경 500m 적용)")
    void createCustomRoutine_andAddTask() {
        Long userId = 777L;
        UserRoutine customRoutine = routineService.createCustomRoutine(userId, "퇴근 후 헬스 루틴", LocalDate.now(), null);

        assertNotNull(customRoutine.getId());
        assertNull(customRoutine.getOriginTemplateId());
        assertEquals("퇴근 후 헬스 루틴", customRoutine.getTitle());

        UserRoutineTaskCreateRequest taskReq = new UserRoutineTaskCreateRequest(
                customRoutine.getId(),
                "헬스장 유산소 30분",
                "19:30",
                "EXERCISE",
                "WEEKDAY",
                101L,
                500, // 500m 슬라이더 반경
                "19:00",
                "운동화 챙기기",
                List.of("MON", "WED", "FRI")
        );

        UserRoutineTask createdTask = routineService.addCustomTask(userId, taskReq);

        assertNotNull(createdTask.getId());
        assertEquals(500, createdTask.getAllowedRadiusMeters());
        assertEquals(LocalTime.of(19, 30), createdTask.getTargetTime());
        assertEquals("WEEKDAY", createdTask.getRepeatType());
    }

    @Test
    @DisplayName("가변 반경(500m) GPS 인증: 300m 통과, 700m 거리 초과 실패 검증")
    void verifyLocation_variableRadius() {
        Long userId = 500L;
        UserProfile profile = new UserProfile(userId, "러너");
        profile.setTotalXp(0);
        profileDb.put(userId, profile);

        Long placeId = 200L;
        placeDb.put(placeId, PlaceResponseDto.builder()
                .placeId(placeId)
                .kakaoPlaceId("kakao-200")
                .name("광화문광장")
                .roadAddress("서울 종로구")
                .coordinates(37.5759, 126.9768)
                .build());

        UserRoutine routine = routineService.createCustomRoutine(userId, "도심 산책", LocalDate.now(), null);
        UserRoutineTask task = routineService.addCustomTask(userId, new UserRoutineTaskCreateRequest(
                routine.getId(), "광화문 산책", "18:00", "WALK", "DAILY", placeId, 500, null, null, null
        ));

        // 1. 반경 초과 (약 1.5km 거리인 서울역 부근 37.555, 126.970) -> 실패 예외
        assertThrows(IllegalStateException.class, () ->
                routineService.completeTask(userId, task.getId(), 37.555, 126.970)
        );

        // 2. 반경 이내 (약 150m 거리 37.5750, 126.9760) -> 통과
        RoutineTaskCompleteResult result = routineService.completeTask(userId, task.getId(), 37.5750, 126.9760);
        assertNotNull(result);
        assertEquals(100, result.earnedXp());
        assertEquals(100, profile.getTotalXp());
    }

    @Test
    @DisplayName("루틴 Task 완료 시 100 XP 지급 및 레벨업(10회 완료 시 1,000 XP -> 2레벨) 검증")
    void completeTask_award100Xp_andLevelUp() {
        Long userId = 300L;
        UserProfile profile = new UserProfile(userId, "성실러");
        profile.setTotalXp(900); // 900 XP 상태
        profileDb.put(userId, profile);

        UserRoutine routine = routineService.createCustomRoutine(userId, "매일 독서", LocalDate.now(), null);
        UserRoutineTask task = routineService.addCustomTask(userId, new UserRoutineTaskCreateRequest(
                routine.getId(), "독서 30분", "21:00", "STUDY", "DAILY", null, 50, null, null, null
        ));

        // Task 실천 완료 -> 100 XP 추가 (총 1,000 XP 달성 -> 2레벨 레벨업)
        RoutineTaskCompleteResult result = routineService.completeTask(userId, task.getId(), null, null);

        assertNotNull(result);
        assertEquals(100, result.earnedXp());
        assertEquals(1000, profile.getTotalXp());
        assertTrue(result.reward().isLevelUp());
        assertEquals(2, result.reward().currentLevel().level());
    }

    @Test
    @DisplayName("당일 이미 완료한 Task를 같은 날 다시 완료 시도시 중복 차단된다")
    void completeTask_preventDuplicateSameDay() {
        Long userId = 200L;
        UserProfile profile = new UserProfile(userId, "열정러");
        profileDb.put(userId, profile);

        UserRoutine routine = routineService.createCustomRoutine(userId, "스트레칭", LocalDate.now(), null);
        UserRoutineTask task = routineService.addCustomTask(userId, new UserRoutineTaskCreateRequest(
                routine.getId(), "자기 전 스트레칭", "23:00", "HEALTH", "DAILY", null, 50, null, null, null
        ));

        // 1회차 성공
        routineService.completeTask(userId, task.getId(), null, null);

        // 2회차 동일 날짜 시도 -> 중복 예외 발생
        IllegalStateException ex = assertThrows(IllegalStateException.class, () ->
                routineService.completeTask(userId, task.getId(), null, null)
        );
        assertTrue(ex.getMessage().contains("이미 오늘 완료한 루틴"));
    }
}
