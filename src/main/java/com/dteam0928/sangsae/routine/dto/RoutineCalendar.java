package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 캘린더 + 요약 카드.
 *
 * @param achievementRate 기간(오늘까지) 완료 수 / 예정 수, % (예정이 없으면 0)
 * @param streakDays      하루 1개 이상 완료한 날이 오늘(오늘 기록이 없으면 어제)까지 이어진 일수
 * @param totalXp         사용자 전체 누적 XP (user_profiles.total_xp)
 */
public record RoutineCalendar(long routineId, String from, String to, List<CalendarDay> days,
                              int achievementRate, int streakDays, int totalXp) {
}
