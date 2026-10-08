package com.dteam0928.sangsae.routine.dto;

/** 루틴 상세 모달의 Task 한 줄. time 은 "HH:mm" */
public record RoutineTaskDto(long id, String content, String time, String category, int rewardXp,
                             String placeName) {
}
