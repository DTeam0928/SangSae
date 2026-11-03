package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.map.dto.LocationAuthResult;
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
import com.dteam0928.sangsae.user.exp.ExpRewardResult;
import com.dteam0928.sangsae.user.exp.ExperienceService;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class RoutineService {

    private final TransactionTemplate tx;
    private final PlaceService placeService;
    private final LocationAuthService locationAuthService;
    private final ExperienceService experienceService;

    private final RoutineTemplateRepository templateRepository;
    private final UserRoutineRepository userRoutineRepository;
    private final RoutineTaskLogRepository logRepository;

    public RoutineService(TransactionTemplate tx,
                          PlaceService placeService,
                          LocationAuthService locationAuthService,
                          ExperienceService experienceService) {
        this(tx, placeService, locationAuthService, experienceService,
             new RoutineTemplateRepository(), new UserRoutineRepository(), new RoutineTaskLogRepository());
    }

    public RoutineService(TransactionTemplate tx,
                          PlaceService placeService,
                          LocationAuthService locationAuthService,
                          ExperienceService experienceService,
                          RoutineTemplateRepository templateRepository,
                          UserRoutineRepository userRoutineRepository,
                          RoutineTaskLogRepository logRepository) {
        this.tx = tx;
        this.placeService = placeService;
        this.locationAuthService = locationAuthService;
        this.experienceService = experienceService;
        this.templateRepository = templateRepository;
        this.userRoutineRepository = userRoutineRepository;
        this.logRepository = logRepository;
    }

    // 1. 공개 루틴 패키지 목록 조회
    public List<RoutineTemplate> getPublicTemplates(String category) {
        return tx.read(em -> templateRepository.findPublicTemplates(em, category));
    }

    // 2. 루틴 패키지 상세 조회 (패키지 정보 + 하위 Task 목록)
    public RoutineTemplateDetailDto getTemplateDetail(Long templateId) {
        return tx.read(em -> {
            RoutineTemplate template = templateRepository.findById(em, templateId);
            if (template == null) {
                return null;
            }
            List<RoutineTemplateTask> tasks = templateRepository.findTasksByTemplateId(em, templateId);
            return new RoutineTemplateDetailDto(template, tasks);
        });
    }

    // 3. 템플릿을 내 루틴으로 담기 (Fork 파이프라인: 패키지 복제 + 하위 Task 일괄 복제)
    public UserRoutine adoptRoutine(Long userId, Long templateId, LocalDate startDate, LocalDate endDate) {
        return tx.inTransaction(em -> {
            RoutineTemplate template = templateRepository.findById(em, templateId);
            if (template == null) {
                throw new IllegalArgumentException("존재하지 않는 루틴 템플릿입니다. ID=" + templateId);
            }

            // 담기 카운트 1 증가
            templateRepository.incrementAdoptedCount(em, templateId);

            // 내 루틴 패키지 생성
            LocalDate actualStart = startDate != null ? startDate : LocalDate.now();
            UserRoutine userRoutine = new UserRoutine(userId, templateId, template.getTitle(), actualStart, endDate);
            userRoutine = userRoutineRepository.saveRoutine(em, userRoutine);

            // 템플릿의 하위 Task들을 내 타임테이블 Task로 일괄 복제
            List<RoutineTemplateTask> templateTasks = templateRepository.findTasksByTemplateId(em, templateId);
            for (RoutineTemplateTask tt : templateTasks) {
                UserRoutineTask userTask = new UserRoutineTask(
                    userRoutine.getId(),
                    tt.getId(),
                    tt.getPlaceId(),
                    tt.getContent(),
                    tt.getDefaultTime(),
                    tt.getCategory(),
                    "DAILY",
                    tt.getRewardXp(),
                    tt.getAllowedRadiusMeters()
                );
                userRoutineRepository.saveTask(em, userTask);
            }

            return userRoutine;
        });
    }

    // 4. 나만의 루틴 직접 생성 (Custom Create)
    public UserRoutine createCustomRoutine(Long userId, String title, LocalDate startDate, LocalDate endDate) {
        if (title == null || title.trim().isEmpty()) {
            throw new IllegalArgumentException("루틴 제목을 입력해주세요.");
        }
        return tx.inTransaction(em -> {
            LocalDate actualStart = startDate != null ? startDate : LocalDate.now();
            UserRoutine userRoutine = new UserRoutine(userId, null, title.trim(), actualStart, endDate);
            return userRoutineRepository.saveRoutine(em, userRoutine);
        });
    }

    // 5. 내 루틴에 세부 Task 직접 추가 (타임테이블 항목 추가 및 가변 인증 반경 지원)
    public UserRoutineTask addCustomTask(Long userId, UserRoutineTaskCreateRequest req) {
        if (req == null || req.userRoutineId() == null) {
            throw new IllegalArgumentException("루틴 ID가 누락되었습니다.");
        }
        if (req.content() == null || req.content().trim().isEmpty()) {
            throw new IllegalArgumentException("Task 내용을 입력해주세요.");
        }

        return tx.inTransaction(em -> {
            UserRoutine userRoutine = userRoutineRepository.findRoutineById(em, req.userRoutineId());
            if (userRoutine == null) {
                throw new IllegalArgumentException("존재하지 않는 사용자 루틴입니다.");
            }
            if (!userId.equals(userRoutine.getUserId())) {
                throw new IllegalArgumentException("본인의 루틴에만 Task를 추가할 수 있습니다.");
            }

            LocalTime targetTime = parseTime(req.targetTime(), LocalTime.of(7, 0));
            LocalTime alertTime = parseTime(req.alertTime(), null);

            int allowedRadius = (req.allowedRadiusMeters() != null && req.allowedRadiusMeters() > 0)
                ? req.allowedRadiusMeters()
                : 50;

            String repeatType = req.repeatType() != null ? req.repeatType().toUpperCase() : "DAILY";

            UserRoutineTask task = new UserRoutineTask(
                userRoutine.getId(),
                null,
                req.placeId(),
                req.content().trim(),
                targetTime,
                req.category() != null ? req.category() : "기타",
                repeatType,
                100,
                allowedRadius
            );
            task.setAlertTime(alertTime);
            task.setMemo(req.memo());

            task = userRoutineRepository.saveTask(em, task);

            // 특정 요일 반복(CUSTOM) 스케줄 저장
            if (req.daysOfWeek() != null && !req.daysOfWeek().isEmpty()) {
                List<RoutineTaskSchedule> schedules = new ArrayList<>();
                for (String day : req.daysOfWeek()) {
                    if (day != null && !day.trim().isEmpty()) {
                        schedules.add(new RoutineTaskSchedule(task.getId(), day.trim().toUpperCase()));
                    }
                }
                userRoutineRepository.saveSchedules(em, schedules);
            }

            return task;
        });
    }

    // 6. 오늘의 타임테이블 조회 (실천 대상 Task 목록 + 장소 정보 + 당일 실천 완료 여부)
    public List<UserRoutineTaskResponseDto> getTodayTimeline(Long userId, LocalDate date) {
        LocalDate targetDate = date != null ? date : LocalDate.now();

        return tx.read(em -> {
            List<UserRoutine> routines = userRoutineRepository.findActiveRoutinesByUserId(em, userId);
            if (routines.isEmpty()) {
                return Collections.emptyList();
            }

            // 운영 기간(startDate ~ endDate) 필터링
            List<UserRoutine> validRoutines = routines.stream()
                .filter(r -> !r.getStartDate().isAfter(targetDate))
                .filter(r -> r.getEndDate() == null || !r.getEndDate().isBefore(targetDate))
                .toList();

            if (validRoutines.isEmpty()) {
                return Collections.emptyList();
            }

            Map<Long, String> routineTitleMap = validRoutines.stream()
                .collect(Collectors.toMap(UserRoutine::getId, UserRoutine::getTitle));

            List<Long> routineIds = validRoutines.stream().map(UserRoutine::getId).toList();
            List<UserRoutineTask> allTasks = userRoutineRepository.findTasksByUserRoutineIds(em, routineIds);
            if (allTasks.isEmpty()) {
                return Collections.emptyList();
            }

            // Task들의 요일 스케줄 조회
            List<Long> taskIds = allTasks.stream().map(UserRoutineTask::getId).toList();
            List<RoutineTaskSchedule> schedules = userRoutineRepository.findSchedulesByTaskIds(em, taskIds);
            Map<Long, List<String>> taskScheduleMap = schedules.stream()
                .collect(Collectors.groupingBy(
                    RoutineTaskSchedule::getUserRoutineTaskId,
                    Collectors.mapping(RoutineTaskSchedule::getDayOfWeek, Collectors.toList())
                ));

            // 당일 요일 계산 (MON, TUE, WED, THU, FRI, SAT, SUN)
            String targetDayOfWeek = targetDate.getDayOfWeek().name().substring(0, 3);
            boolean isWeekend = targetDate.getDayOfWeek() == DayOfWeek.SATURDAY || targetDate.getDayOfWeek() == DayOfWeek.SUNDAY;

            // 당일 요일 조건 부합 여부 필터링
            List<UserRoutineTask> matchedTasks = allTasks.stream().filter(task -> {
                String repType = task.getRepeatType() != null ? task.getRepeatType().toUpperCase() : "DAILY";
                if ("DAILY".equals(repType)) {
                    return true;
                }
                if ("WEEKDAY".equals(repType)) {
                    return !isWeekend;
                }
                if ("WEEKEND".equals(repType)) {
                    return isWeekend;
                }
                List<String> days = taskScheduleMap.get(task.getId());
                return days != null && days.contains(targetDayOfWeek);
            }).toList();

            if (matchedTasks.isEmpty()) {
                return Collections.emptyList();
            }

            // 당일 실천 완료 로그 조회
            List<RoutineTaskLog> todayLogs = logRepository.findTodayLogsByUser(em, userId, targetDate);
            Set<Long> doneTaskIdSet = todayLogs.stream()
                .map(RoutineTaskLog::getUserRoutineTaskId)
                .collect(Collectors.toSet());

            // DTO 조립 (시간순 정렬)
            List<UserRoutineTaskResponseDto> responseDtos = new ArrayList<>();
            for (UserRoutineTask task : matchedTasks) {
                String placeName = null;
                String roadAddress = null;
                Double lat = null;
                Double lng = null;

                if (task.getPlaceId() != null && placeService != null) {
                    PlaceResponseDto place = placeService.getPlaceById(task.getPlaceId());
                    if (place != null) {
                        placeName = place.name();
                        roadAddress = place.roadAddress();
                        lat = place.latitude();
                        lng = place.longitude();
                    }
                }

                boolean isDone = doneTaskIdSet.contains(task.getId());
                List<String> days = taskScheduleMap.getOrDefault(task.getId(), Collections.emptyList());

                responseDtos.add(new UserRoutineTaskResponseDto(
                    task.getId(),
                    task.getUserRoutineId(),
                    routineTitleMap.get(task.getUserRoutineId()),
                    task.getContent(),
                    task.getTargetTime(),
                    task.getCategory(),
                    task.getRepeatType(),
                    days,
                    task.getPlaceId(),
                    placeName,
                    roadAddress,
                    lat,
                    lng,
                    task.getAllowedRadiusMeters(),
                    task.getRewardXp(),
                    task.getMemo(),
                    task.isActive(),
                    isDone
                ));
            }

            responseDtos.sort(Comparator.comparing(UserRoutineTaskResponseDto::targetTime)
                .thenComparing(UserRoutineTaskResponseDto::id));

            return responseDtos;
        });
    }

    // 7. 루틴 Task 실천 완료 처리 (GPS 가변 반경 검증 + 100 XP 보상 & 레벨업 판정)
    public RoutineTaskCompleteResult completeTask(Long userId, Long taskId, Double userLat, Double userLng) {
        return tx.inTransaction(em -> {
            UserRoutineTask task = userRoutineRepository.findTaskById(em, taskId);
            if (task == null) {
                throw new IllegalArgumentException("존재하지 않는 루틴 Task입니다. ID=" + taskId);
            }

            UserRoutine routine = userRoutineRepository.findRoutineById(em, task.getUserRoutineId());
            if (routine == null || !userId.equals(routine.getUserId())) {
                throw new IllegalArgumentException("본인의 루틴 Task만 완료할 수 있습니다.");
            }

            LocalDate today = LocalDate.now();
            LocalTime nowTime = LocalTime.now();

            // 당일 중복 실천 방지 검증
            RoutineTaskLog existingLog = logRepository.findTodayLog(em, userId, taskId, today);
            if (existingLog != null) {
                throw new IllegalStateException("이미 오늘 완료한 루틴 Task입니다.");
            }

            // 장소 기반 Task인 경우 가변 반경(50m~1km) GPS 거리 검증
            if (task.getPlaceId() != null) {
                if (userLat == null || userLng == null) {
                    throw new IllegalArgumentException("현장 방문 인증을 위한 GPS 좌표가 누락되었습니다.");
                }
                int allowedRadius = task.getAllowedRadiusMeters() > 0 ? task.getAllowedRadiusMeters() : 50;

                LocationAuthResult authResult = locationAuthService.verifyLocation(
                    task.getPlaceId(), userLat, userLng, allowedRadius
                );

                if (!authResult.isSuccess()) {
                    throw new IllegalStateException(authResult.message());
                }
            }

            // 실천 원장 로그 저장
            RoutineTaskLog log = new RoutineTaskLog(
                userId,
                taskId,
                today,
                nowTime,
                null,
                task.getRewardXp()
            );
            log = logRepository.save(em, log);

            // 경험치(100 XP) 지급 및 레벨업/칭호 판정
            ExpRewardResult rewardResult = null;
            if (experienceService != null) {
                rewardResult = experienceService.awardRoutineExp(em, userId, task.getRewardXp());
            }

            return new RoutineTaskCompleteResult(
                taskId,
                log.getId(),
                today,
                nowTime,
                task.getRewardXp(),
                rewardResult
            );
        });
    }

    private LocalTime parseTime(String timeStr, LocalTime defaultTime) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return defaultTime;
        }
        try {
            String trimmed = timeStr.trim();
            if (trimmed.length() == 5) {
                return LocalTime.parse(trimmed, DateTimeFormatter.ofPattern("HH:mm"));
            }
            return LocalTime.parse(trimmed);
        } catch (Exception e) {
            return defaultTime;
        }
    }
}
