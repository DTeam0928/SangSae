package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.db.TransactionTemplate;
import com.dteam0928.sangsae.common.exception.BusinessException;
import com.dteam0928.sangsae.common.exception.ErrorCode;
import com.dteam0928.sangsae.routine.dto.CalendarDay;
import com.dteam0928.sangsae.routine.dto.ManageHome;
import com.dteam0928.sangsae.routine.dto.MyRoutine;
import com.dteam0928.sangsae.routine.dto.MyTask;
import com.dteam0928.sangsae.routine.dto.RoutineCalendar;
import com.dteam0928.sangsae.routine.dto.RoutineCategoryDto;
import com.dteam0928.sangsae.routine.dto.RoutineForm;
import com.dteam0928.sangsae.routine.dto.TaskForm;
import com.dteam0928.sangsae.routine.dto.Timeline;
import com.dteam0928.sangsae.routine.repository.LogRow;
import com.dteam0928.sangsae.routine.repository.MyPlaceRepository;
import com.dteam0928.sangsae.routine.repository.MyRoutineRepository;
import com.dteam0928.sangsae.routine.repository.MyRoutineRepository.PublicInfo;
import com.dteam0928.sangsae.routine.repository.MyRoutineRepository.TaskValues;
import com.dteam0928.sangsae.routine.repository.RoutineExploreRepository;
import com.dteam0928.sangsae.routine.repository.RoutineRow;
import com.dteam0928.sangsae.routine.repository.TaskRow;
import jakarta.persistence.EntityManager;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * R-00 루틴 관리 탭.
 * <ul>
 *   <li>삭제는 숨김(is_active = 0) → 완료 기록·XP 이력 보존</li>
 *   <li>공개 루틴 = 탐색용 사본(routine_templates, 만든 사람 = 나)과 연결된 내 루틴.
 *       Task 를 바꾸면 사본 Task 도 같이 바꾼다. 다른 사용자가 추가해 간 Task 는 영향 없음</li>
 *   <li>완료 체크는 팀원 API(POST /user-routines/tasks/complete)를 그대로 쓴다 (GPS 인증·XP 지급, 해제 불가)</li>
 * </ul>
 */
public class MyRoutineService {

    private static final int TITLE_MAX = 100;
    private static final int DESCRIPTION_MAX = 500;
    private static final int CATEGORY_MAX = 3;
    private static final int TAG_MAX = 5;
    private static final int TAG_LENGTH_MAX = 20;
    private static final int CONTENT_MAX = 150;
    private static final int MEMO_MAX = 255;
    private static final int CALENDAR_MAX_DAYS = 42;   // 월 보기 6주
    private static final int STREAK_LOOKBACK = 400;

    private final TransactionTemplate tx;
    private final MyRoutineRepository repo;
    private final RoutineExploreRepository exploreRepository;
    private final MyPlaceRepository myPlaceRepository;
    private final RoutineSettings settings;
    private final Clock clock;

    public MyRoutineService(TransactionTemplate tx, MyRoutineRepository repo, RoutineExploreRepository exploreRepository,
                            MyPlaceRepository myPlaceRepository, RoutineSettings settings) {
        this(tx, repo, exploreRepository, myPlaceRepository, settings, Clock.systemDefaultZone());
    }

    public MyRoutineService(TransactionTemplate tx, MyRoutineRepository repo, RoutineExploreRepository exploreRepository,
                            MyPlaceRepository myPlaceRepository, RoutineSettings settings, Clock clock) {
        this.tx = tx;
        this.repo = repo;
        this.exploreRepository = exploreRepository;
        this.myPlaceRepository = myPlaceRepository;
        this.settings = settings;
        this.clock = clock;
    }

    // ================================================================== 조회

    /** 첫 로딩: 루틴 셀렉트 + 카테고리·장소 선택지 */
    public ManageHome home(long userId) {
        return tx.read(em -> homeOf(em, userId));
    }

    public MyRoutine detail(long userId, long routineId) {
        return tx.read(em -> toDto(em, userId, routine(em, userId, routineId)));
    }

    /** 선택한 날짜(없으면 오늘)의 타임테이블 */
    public Timeline timeline(long userId, long routineId, String date) {
        LocalDate day = parseDate(date, today(), ErrorCode.INVALID_INPUT);
        return tx.read(em -> timelineOf(em, userId, routine(em, userId, routineId), day));
    }

    /** 캘린더 + 요약 카드. 기간이 없으면 오늘이 있는 주(일~토) */
    public RoutineCalendar calendar(long userId, long routineId, String from, String to) {
        LocalDate sunday = today().with(TemporalAdjusters.previousOrSame(DayOfWeek.SUNDAY));
        LocalDate start = parseDate(from, sunday, ErrorCode.INVALID_INPUT);
        LocalDate end = parseDate(to, start.plusDays(6), ErrorCode.INVALID_INPUT);
        if (end.isBefore(start) || ChronoUnit.DAYS.between(start, end) >= CALENDAR_MAX_DAYS) {
            throw new BusinessException(ErrorCode.INVALID_INPUT);
        }
        LocalDate today = today();
        return tx.read(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            List<TaskRow> tasks = repo.findTasks(em, routineId);
            Map<LocalDate, Set<Long>> doneByDate = new HashMap<>();
            for (LogRow log : repo.findLogs(em, userId, routineId, start, end)) {
                doneByDate.computeIfAbsent(log.date(), d -> new java.util.HashSet<>()).add(log.taskId());
            }

            List<CalendarDay> days = new ArrayList<>();
            int plannedSum = 0;
            int doneSum = 0;
            for (LocalDate d = start; !d.isAfter(end); d = d.plusDays(1)) {
                int planned = 0;
                int done = 0;
                if (routine.covers(d)) {
                    Set<Long> doneIds = doneByDate.getOrDefault(d, Set.of());
                    for (TaskRow t : tasks) {
                        if (!RepeatRule.plannedOn(t, d)) continue;
                        planned++;
                        if (doneIds.contains(t.id())) done++;
                    }
                }
                days.add(new CalendarDay(d.toString(), planned, done));
                if (!d.isAfter(today)) {          // 달성률은 오늘까지만 (미래 예정은 빼고)
                    plannedSum += planned;
                    doneSum += done;
                }
            }
            int rate = plannedSum == 0 ? 0 : (int) Math.round(doneSum * 100.0 / plannedSum);
            int streak = streak(repo.findLogDates(em, userId, routineId, today, STREAK_LOOKBACK), today);
            return new RoutineCalendar(routineId, start.toString(), end.toString(), days, rate, streak, repo.findTotalXp(em, userId));
        });
    }

    /** 최근 순 날짜 목록에서, 오늘(없으면 어제)부터 하루씩 거꾸로 이어진 일수 */
    static int streak(List<LocalDate> datesDesc, LocalDate today) {
        if (datesDesc.isEmpty()) return 0;
        LocalDate expected = datesDesc.get(0).equals(today) ? today : today.minusDays(1);
        int count = 0;
        for (LocalDate d : datesDesc) {
            if (!d.equals(expected)) break;
            count++;
            expected = expected.minusDays(1);
        }
        return count;
    }

    // ================================================================== 루틴 생성·수정·삭제

    public MyRoutine create(long userId, RoutineForm form) {
        return tx.inTransaction(em -> {
            ValidRoutine v = validate(em, form);
            long routineId = repo.insertRoutine(em, userId, null, v.title, v.start, v.end);
            if (v.isPublic()) {
                long templateId = repo.insertTemplate(em, userId, v.publicInfo);
                repo.linkTemplate(em, routineId, templateId);
            }
            return toDto(em, userId, routine(em, userId, routineId));
        });
    }

    /** 기본 정보 수정. 공개로 바꾸면 그때까지의 Task 를 탐색용 사본에 복사한다 */
    public MyRoutine update(long userId, long routineId, RoutineForm form) {
        return tx.inTransaction(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            ValidRoutine v = validate(em, form);
            if (v.isPublic() && routine.adopted(userId)) throw new BusinessException(ErrorCode.ROUTINE_VISIBILITY_LOCKED);
            repo.updateRoutine(em, routineId, v.title, v.start, v.end);

            Long own = routine.ownTemplateId(userId);
            if (own != null) {
                if (v.publicInfo != null) repo.updateTemplate(em, own, v.publicInfo);
                else repo.setTemplateVisibility(em, own, "PRIVATE");   // 비공개 + 카테고리 없음 → 숨기기만
            } else if (v.isPublic()) {
                long templateId = repo.insertTemplate(em, userId, v.publicInfo);
                repo.linkTemplate(em, routineId, templateId);
                for (TaskRow t : repo.findTasks(em, routineId)) {
                    if (!mirrored(t.repeatType())) continue;
                    long copyId = repo.insertTemplateTask(em, templateId, placeId(t), t.content(),
                            LocalTime.parse(t.time()), t.categoryName());
                    repo.linkTemplateTask(em, t.id(), copyId);
                }
                repo.resequenceTemplateTasks(em, templateId);
            }
            return toDto(em, userId, routine(em, userId, routineId));
        });
    }

    /** 숨김 처리. 공개 중이던 사본도 탐색 탭에서 내린다 (추가해 간 사용자의 루틴은 그대로) */
    public ManageHome delete(long userId, long routineId) {
        return tx.inTransaction(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            repo.deactivateRoutine(em, routineId);
            Long own = routine.ownTemplateId(userId);
            if (own != null) repo.setTemplateVisibility(em, own, "PRIVATE");
            return homeOf(em, userId);
        });
    }

    // ================================================================== Task 등록·수정·삭제 (응답: date 의 타임테이블)

    public Timeline addTask(long userId, long routineId, TaskForm form, String date) {
        LocalDate day = parseDate(date, today(), ErrorCode.INVALID_INPUT);
        return tx.inTransaction(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            ValidTask v = validate(em, routine, form);
            long taskId = repo.insertTask(em, routineId, v.values);
            repo.replaceSchedules(em, taskId, v.days);
            syncTemplateTask(em, routine.ownTemplateId(userId), taskId, null, v.values);
            return timelineOf(em, userId, routine, day);
        });
    }

    public Timeline updateTask(long userId, long routineId, long taskId, TaskForm form, String date) {
        LocalDate day = parseDate(date, today(), ErrorCode.INVALID_INPUT);
        return tx.inTransaction(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            TaskRow task = task(em, routineId, taskId);
            ValidTask v = validate(em, routine, form);
            repo.updateTask(em, taskId, v.values);
            repo.replaceSchedules(em, taskId, v.days);
            syncTemplateTask(em, routine.ownTemplateId(userId), taskId, task.originTemplateTaskId(), v.values);
            return timelineOf(em, userId, routine, day);
        });
    }

    public Timeline deleteTask(long userId, long routineId, long taskId, String date) {
        LocalDate day = parseDate(date, today(), ErrorCode.INVALID_INPUT);
        return tx.inTransaction(em -> {
            RoutineRow routine = routine(em, userId, routineId);
            TaskRow task = task(em, routineId, taskId);
            repo.deactivateTask(em, taskId);
            Long own = routine.ownTemplateId(userId);
            if (own != null && task.originTemplateTaskId() != null) {
                repo.deleteTemplateTask(em, own, task.originTemplateTaskId());
                repo.resequenceTemplateTasks(em, own);
            }
            return timelineOf(em, userId, routine, day);
        });
    }

    /**
     * 공개용 사본 Task 맞추기. 사본(own)이 있을 때만.
     * 1회성 Task 는 사본에 넣지 않는다 (사본에는 반복 정보가 없어 추가해 간 사람에게 매일 Task 가 되므로).
     */
    private void syncTemplateTask(EntityManager em, Long own, long taskId, Long copyId, TaskValues v) {
        if (own == null) return;
        if (mirrored(v.repeatType())) {
            if (copyId != null) {
                repo.updateTemplateTask(em, own, copyId, v.placeId(), v.content(), v.time(), v.category());
            } else {
                long newCopy = repo.insertTemplateTask(em, own, v.placeId(), v.content(), v.time(), v.category());
                repo.linkTemplateTask(em, taskId, newCopy);
            }
        } else if (copyId != null) {
            repo.deleteTemplateTask(em, own, copyId);   // FK 가 내 Task 의 연결도 NULL 로 만든다
        }
        repo.resequenceTemplateTasks(em, own);
    }

    private static boolean mirrored(String repeatType) {
        return !"ONCE".equals(repeatType);
    }

    // ================================================================== 공통

    private ManageHome homeOf(EntityManager em, long userId) {
        List<RoutineCategoryDto> categories = exploreRepository.findCategories(em);
        List<MyRoutine> routines = repo.findRoutines(em, userId).stream()
                .map(r -> toDto(userId, r, categories)).toList();
        return new ManageHome(routines, categories, myPlaceRepository.findMyPlaces(em, userId, null));
    }

    private Timeline timelineOf(EntityManager em, long userId, RoutineRow routine, LocalDate day) {
        if (!routine.covers(day)) return new Timeline(routine.id(), day.toString(), List.of(), 0);
        Map<Long, String> doneAt = new HashMap<>();
        for (LogRow log : repo.findLogs(em, userId, routine.id(), day, day)) doneAt.put(log.taskId(), log.completedAt());

        List<MyTask> tasks = repo.findTasks(em, routine.id()).stream()
                .filter(t -> RepeatRule.plannedOn(t, day))
                .map(t -> new MyTask(t.id(), t.content(), t.time(), t.categoryCode(), t.categoryName(), t.categoryEmoji(),
                        t.repeatType(), RepeatRule.label(t), t.daysOfWeek(), str(t.onceDate()), t.alertTime(), t.memo(),
                        t.place(), doneAt.containsKey(t.id()), doneAt.get(t.id())))
                .toList();
        int done = (int) tasks.stream().filter(MyTask::done).count();
        return new Timeline(routine.id(), day.toString(), tasks, done);
    }

    private MyRoutine toDto(EntityManager em, long userId, RoutineRow r) {
        return toDto(userId, r, exploreRepository.findCategories(em));
    }

    private static MyRoutine toDto(long userId, RoutineRow r, List<RoutineCategoryDto> all) {
        boolean own = r.ownTemplateId(userId) != null;
        List<RoutineCategoryDto> categories = all.stream().filter(c -> r.categoryCodes().contains(c.getCode())).toList();
        return new MyRoutine(r.id(), r.title(), str(r.startDate()), str(r.endDate()),
                own && "PUBLIC".equals(r.visibility()) ? "PUBLIC" : "PRIVATE", r.adopted(userId),
                r.description(), categories, r.tags(), r.representativePlace(), r.taskCount());
    }

    private RoutineRow routine(EntityManager em, long userId, long routineId) {
        RoutineRow r = repo.findRoutine(em, userId, routineId);
        if (r == null) throw new BusinessException(ErrorCode.ROUTINE_NOT_FOUND);
        return r;
    }

    private TaskRow task(EntityManager em, long routineId, long taskId) {
        TaskRow t = repo.findTask(em, routineId, taskId);
        if (t == null) throw new BusinessException(ErrorCode.TASK_NOT_FOUND);
        return t;
    }

    private static Long placeId(TaskRow t) {
        return t.place() == null ? null : t.place().id();
    }

    private LocalDate today() {
        return LocalDate.now(clock);
    }

    // ================================================================== 검증

    /** publicInfo: 공개이거나 카테고리를 보냈을 때만 (탐색용 사본에 쓸 값) */
    private record ValidRoutine(String title, LocalDate start, LocalDate end, String visibility, PublicInfo publicInfo) {
        boolean isPublic() {
            return "PUBLIC".equals(visibility);
        }
    }

    private ValidRoutine validate(EntityManager em, RoutineForm f) {
        String title = trim(f.title());
        if (title == null || title.length() > TITLE_MAX) throw new BusinessException(ErrorCode.ROUTINE_TITLE_INVALID, TITLE_MAX);
        LocalDate start = parseDate(f.startDate(), today(), ErrorCode.ROUTINE_PERIOD_INVALID);
        LocalDate end = parseDate(f.endDate(), null, ErrorCode.ROUTINE_PERIOD_INVALID);
        if (end != null && end.isBefore(start)) throw new BusinessException(ErrorCode.ROUTINE_PERIOD_INVALID);

        String visibility = trim(f.visibility()) == null ? "PRIVATE" : f.visibility().trim().toUpperCase();
        if (!visibility.equals("PUBLIC") && !visibility.equals("PRIVATE")) throw new BusinessException(ErrorCode.INVALID_INPUT);

        List<String> codes = distinct(f.categories());
        if (!visibility.equals("PUBLIC") && codes.isEmpty()) return new ValidRoutine(title, start, end, visibility, null);

        Set<String> known = new java.util.HashSet<>();
        exploreRepository.findCategories(em).forEach(c -> known.add(c.getCode()));
        if (codes.isEmpty() || codes.size() > CATEGORY_MAX || !known.containsAll(codes)) {
            throw new BusinessException(ErrorCode.ROUTINE_CATEGORY_INVALID, CATEGORY_MAX);
        }
        List<String> tags = new ArrayList<>();
        for (String t : distinct(f.tags())) {
            String tag = t.startsWith("#") ? t.substring(1).trim() : t;
            if (tag.isEmpty()) continue;
            if (tag.length() > TAG_LENGTH_MAX) throw new BusinessException(ErrorCode.ROUTINE_TAG_INVALID, TAG_MAX, TAG_LENGTH_MAX);
            if (!tags.contains(tag)) tags.add(tag);
        }
        if (tags.size() > TAG_MAX) throw new BusinessException(ErrorCode.ROUTINE_TAG_INVALID, TAG_MAX, TAG_LENGTH_MAX);
        String description = trim(f.description());
        if (description != null && description.length() > DESCRIPTION_MAX) {
            throw new BusinessException(ErrorCode.ROUTINE_DESCRIPTION_INVALID, DESCRIPTION_MAX);
        }
        Long placeId = f.representativePlaceId();
        if (placeId != null && !repo.existsPlace(em, placeId)) throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);

        return new ValidRoutine(title, start, end, visibility,
                new PublicInfo(title, description, codes, tags, placeId, visibility));
    }

    /** days: CUSTOM 일 때 요일(MON~SUN 순), 아니면 빈 목록 */
    private record ValidTask(TaskValues values, List<String> days) {
    }

    private ValidTask validate(EntityManager em, RoutineRow routine, TaskForm f) {
        String content = trim(f.content());
        if (content == null || content.length() > CONTENT_MAX) throw new BusinessException(ErrorCode.TASK_CONTENT_INVALID, CONTENT_MAX);
        LocalTime time = parseTime(f.time(), ErrorCode.TASK_TIME_INVALID);
        if (time == null) throw new BusinessException(ErrorCode.TASK_TIME_INVALID);

        String code = trim(f.category());
        String categoryName = code == null ? null : exploreRepository.findCategories(em).stream()
                .filter(c -> c.getCode().equals(code)).map(RoutineCategoryDto::getName).findFirst().orElse(null);
        if (categoryName == null) throw new BusinessException(ErrorCode.TASK_CATEGORY_INVALID);

        if (f.placeId() != null && !repo.existsPlace(em, f.placeId())) throw new BusinessException(ErrorCode.PLACE_NOT_FOUND);

        String type = trim(f.repeatType()) == null ? "DAILY" : f.repeatType().trim().toUpperCase();
        if (!RepeatRule.TYPES.contains(type)) throw new BusinessException(ErrorCode.TASK_REPEAT_INVALID);
        List<String> days = List.of();
        LocalDate onceDate = null;
        if (type.equals("CUSTOM")) {
            List<String> picked = distinct(f.daysOfWeek()).stream().map(String::toUpperCase).toList();
            if (picked.isEmpty() || !RepeatRule.DAYS.containsAll(picked)) throw new BusinessException(ErrorCode.TASK_REPEAT_INVALID);
            days = RepeatRule.DAYS.stream().filter(picked::contains).toList();
        } else if (type.equals("ONCE")) {
            onceDate = parseDate(f.onceDate(), null, ErrorCode.TASK_REPEAT_INVALID);
            if (onceDate == null || !routine.covers(onceDate)) throw new BusinessException(ErrorCode.TASK_REPEAT_INVALID);
        }

        LocalTime alert = parseTime(f.alertTime(), ErrorCode.TASK_ALERT_INVALID);
        String memo = trim(f.memo());
        if (memo != null && memo.length() > MEMO_MAX) throw new BusinessException(ErrorCode.TASK_MEMO_INVALID, MEMO_MAX);

        return new ValidTask(new TaskValues(content, time, categoryName, f.placeId(), type, onceDate, alert, memo,
                settings.taskRewardXp()), days);
    }

    private static LocalDate parseDate(String s, LocalDate whenBlank, ErrorCode error) {
        String v = trim(s);
        if (v == null) return whenBlank;
        try {
            return LocalDate.parse(v);
        } catch (DateTimeParseException e) {
            throw new BusinessException(error);
        }
    }

    /** "HH:mm" (또는 HH:mm:ss). 비어 있으면 null */
    private static LocalTime parseTime(String s, ErrorCode error) {
        String v = trim(s);
        if (v == null) return null;
        try {
            return LocalTime.parse(v).withSecond(0).withNano(0);
        } catch (DateTimeParseException e) {
            throw new BusinessException(error);
        }
    }

    private static String str(LocalDate d) {
        return d == null ? null : d.toString();
    }

    private static String trim(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private static List<String> distinct(List<String> values) {
        if (values == null) return List.of();
        LinkedHashSet<String> set = new LinkedHashSet<>();
        for (String v : values) {
            if (v != null && !v.isBlank()) set.add(v.trim());
        }
        return List.copyOf(set);
    }
}
