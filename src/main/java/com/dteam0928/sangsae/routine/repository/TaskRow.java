package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.routine.dto.PlaceSummary;

import java.time.LocalDate;
import java.util.List;

/**
 * 활성 Task 한 줄.
 *
 * @param time                 "HH:mm"
 * @param categoryName         user_routine_tasks.category (카테고리 이름이 저장돼 있다)
 * @param categoryCode         이름으로 찾은 routine_categories.code (없으면 null)
 * @param createdDate          만든 날. 그 전 날짜에는 예정으로 치지 않는다
 * @param originTemplateTaskId 공개용 사본 Task id (없으면 null)
 * @param daysOfWeek           routine_task_schedules (CUSTOM)
 */
public record TaskRow(long id, String content, String time, String categoryName, String categoryCode,
                      String categoryEmoji, String repeatType, LocalDate onceDate, String alertTime, String memo,
                      LocalDate createdDate, Long originTemplateTaskId, List<String> daysOfWeek, PlaceSummary place) {
}
