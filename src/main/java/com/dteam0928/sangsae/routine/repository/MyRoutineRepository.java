package com.dteam0928.sangsae.routine.repository;

import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * 루틴 관리 탭 (user_routines, user_routine_tasks, routine_task_schedules, routine_task_logs)
 * + 공개 루틴의 탐색용 사본 (routine_templates 와 하위 테이블).
 * 삭제는 모두 is_active = 0 (기록 보관). 탐색용 사본 Task 만 실제로 지운다.
 */
public interface MyRoutineRepository {

    // ---- 루틴
    /** 활성 루틴, 만든 순 */
    List<RoutineRow> findRoutines(EntityManager em, long userId);

    /** 내 활성 루틴이 아니면 null */
    RoutineRow findRoutine(EntityManager em, long userId, long routineId);

    long insertRoutine(EntityManager em, long userId, Long templateId, String title, LocalDate start, LocalDate end);

    void updateRoutine(EntityManager em, long routineId, String title, LocalDate start, LocalDate end);

    void linkTemplate(EntityManager em, long routineId, long templateId);

    void deactivateRoutine(EntityManager em, long routineId);

    // ---- 탐색용 사본 (routine_templates)
    long insertTemplate(EntityManager em, long creatorId, PublicInfo info);

    void updateTemplate(EntityManager em, long templateId, PublicInfo info);

    void setTemplateVisibility(EntityManager em, long templateId, String visibility);

    /** 템플릿 Task 를 추가하고 id 반환. 정렬 순서는 {@link #resequenceTemplateTasks} 로 맞춘다 */
    long insertTemplateTask(EntityManager em, long templateId, Long placeId, String content, LocalTime time, String category);

    void updateTemplateTask(EntityManager em, long templateId, long templateTaskId, Long placeId, String content,
                            LocalTime time, String category);

    /** 다른 사용자가 추가해 간 Task 는 FK(ON DELETE SET NULL)로 연결만 끊기고 남는다 */
    void deleteTemplateTask(EntityManager em, long templateId, long templateTaskId);

    /** sort_order 를 시간순으로 다시 매긴다 (탐색 탭 상세 모달 순서) */
    void resequenceTemplateTasks(EntityManager em, long templateId);

    // ---- Task
    List<TaskRow> findTasks(EntityManager em, long routineId);

    TaskRow findTask(EntityManager em, long routineId, long taskId);

    long insertTask(EntityManager em, long routineId, TaskValues v);

    void updateTask(EntityManager em, long taskId, TaskValues v);

    void linkTemplateTask(EntityManager em, long taskId, long templateTaskId);

    void deactivateTask(EntityManager em, long taskId);

    void replaceSchedules(EntityManager em, long taskId, List<String> daysOfWeek);

    // ---- 기록·통계
    List<LogRow> findLogs(EntityManager em, long userId, long routineId, LocalDate from, LocalDate to);

    /** 완료 기록이 있는 날짜, 최근 순 (to 이전, 최대 limit 개) */
    List<LocalDate> findLogDates(EntityManager em, long userId, long routineId, LocalDate to, int limit);

    int findTotalXp(EntityManager em, long userId);

    // ---- 검증
    boolean existsPlace(EntityManager em, long placeId);

    /** 탐색 탭에 보여줄 공개 정보. mainCategory = routine_templates.category (첫 번째 카테고리) */
    record PublicInfo(String title, String description, List<String> categoryCodes, List<String> tags,
                      Long representativePlaceId, String visibility) {

        public String mainCategory() {
            return categoryCodes.isEmpty() ? null : categoryCodes.get(0);
        }
    }

    /** Task 저장값 (검증 끝난 값). category 는 카테고리 이름 */
    record TaskValues(String content, LocalTime time, String category, Long placeId, String repeatType,
                      LocalDate onceDate, LocalTime alertTime, String memo, int rewardXp) {
    }
}
