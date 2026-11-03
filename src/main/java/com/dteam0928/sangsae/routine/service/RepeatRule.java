package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.routine.repository.TaskRow;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Task 반복 규칙. 팀원 코드(RoutineService)와 같은 값을 쓴다: DAILY / WEEKDAY / WEEKEND / CUSTOM(요일) / ONCE(1회).
 * 요일 코드는 MON~SUN.
 */
public final class RepeatRule {

    public static final List<String> TYPES = List.of("DAILY", "WEEKDAY", "WEEKEND", "CUSTOM", "ONCE");
    public static final List<String> DAYS = List.of("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN");
    private static final List<String> DAY_NAMES = List.of("월", "화", "수", "목", "금", "토", "일");

    private RepeatRule() {
    }

    /** 그 날짜에 해야 하는 Task 인가 (루틴 운영 기간은 따로 확인). 만들기 전 날짜는 제외 (1회성은 실행일만 본다) */
    public static boolean plannedOn(TaskRow task, LocalDate date) {
        String type = task.repeatType() == null ? "DAILY" : task.repeatType();
        if ("ONCE".equals(type)) return date.equals(task.onceDate());
        if (task.createdDate() != null && date.isBefore(task.createdDate())) return false;
        DayOfWeek dow = date.getDayOfWeek();
        boolean weekend = dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY;
        return switch (type) {
            case "WEEKDAY" -> !weekend;
            case "WEEKEND" -> weekend;
            case "CUSTOM" -> task.daysOfWeek().contains(code(dow));
            default -> true;
        };
    }

    /** "매일 반복", "평일 반복", "주말 반복", "매주 월·수·금", "1회 · 10월 8일" */
    public static String label(TaskRow task) {
        String type = task.repeatType() == null ? "DAILY" : task.repeatType();
        return switch (type) {
            case "DAILY" -> "매일 반복";
            case "WEEKDAY" -> "평일 반복";
            case "WEEKEND" -> "주말 반복";
            case "CUSTOM" -> "매주 " + task.daysOfWeek().stream().filter(DAYS::contains)
                    .sorted((a, b) -> DAYS.indexOf(a) - DAYS.indexOf(b))
                    .map(d -> DAY_NAMES.get(DAYS.indexOf(d))).collect(Collectors.joining("·"));
            case "ONCE" -> task.onceDate() == null ? "1회"
                    : "1회 · " + task.onceDate().getMonthValue() + "월 " + task.onceDate().getDayOfMonth() + "일";
            default -> type;
        };
    }

    static String code(DayOfWeek dow) {
        return DAYS.get(dow.getValue() - 1);
    }
}
