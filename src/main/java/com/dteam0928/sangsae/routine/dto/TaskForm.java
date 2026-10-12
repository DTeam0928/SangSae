package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * Task 등록·수정 입력값 (검증은 서비스에서).
 *
 * @param time       "HH:mm" (필수)
 * @param category   routine_categories.code (필수)
 * @param repeatType DAILY / WEEKDAY / WEEKEND / CUSTOM / ONCE (없으면 DAILY)
 * @param daysOfWeek CUSTOM 일 때 MON~SUN
 * @param onceDate   ONCE 일 때 "yyyy-MM-dd"
 * @param allowedRadiusMeters 장소 Task 의 GPS 인증 허용 반경(m), 없으면 50
 */
public record TaskForm(String content, String time, String category, Long placeId,
                       String repeatType, List<String> daysOfWeek, String onceDate,
                       String alertTime, String memo, Integer allowedRadiusMeters) {
}
