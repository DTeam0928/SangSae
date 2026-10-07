package com.dteam0928.sangsae.routine.dto;

import java.time.LocalTime;
import java.util.List;

public record UserRoutineTaskResponseDto(
    Long id,
    Long userRoutineId,
    String routineTitle,
    String content,
    LocalTime targetTime,
    String category,
    String repeatType,
    List<String> daysOfWeek,
    Long placeId,
    String placeName,
    String roadAddress,
    Double latitude,
    Double longitude,
    int allowedRadiusMeters,
    int rewardXp,
    String memo,
    boolean isActive,
    boolean isDoneToday
) {
}
