package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 타임테이블의 Task 한 줄 (+ Task 설정 폼 채우기용 값).
 *
 * @param time          "19:00"
 * @param categoryCode  routine_categories.code (카테고리 이름이 목록에 없으면 null)
 * @param repeatType    DAILY / WEEKDAY / WEEKEND / CUSTOM / ONCE
 * @param repeatLabel   "매일 반복", "매주 월·수·금", "1회 · 10월 8일" 등 화면 표시용
 * @param daysOfWeek    CUSTOM 일 때 요일 (MON~SUN)
 * @param onceDate      ONCE 일 때 실행일 "yyyy-MM-dd"
 * @param done          조회한 날짜에 완료했는지
 * @param completedAt   완료 시각 "19:03" (미완료면 null)
 */
public record MyTask(long id, String content, String time,
                     String categoryCode, String categoryName, String categoryEmoji,
                     String repeatType, String repeatLabel, List<String> daysOfWeek, String onceDate,
                     String alertTime, String memo, PlaceSummary place,
                     boolean done, String completedAt) {
}
