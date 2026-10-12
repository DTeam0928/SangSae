package com.dteam0928.sangsae.routine.dto;

/** 캘린더 하루: 예정 Task 수(점 개수)와 완료 수. date = "yyyy-MM-dd" */
public record CalendarDay(String date, int planned, int done) {
}
