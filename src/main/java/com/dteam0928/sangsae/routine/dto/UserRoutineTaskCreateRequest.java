package com.dteam0928.sangsae.routine.dto;

import java.util.List;

public record UserRoutineTaskCreateRequest(
    Long userRoutineId,
    String content,
    String targetTime,
    String category,
    String repeatType,
    Long placeId,
    Integer allowedRadiusMeters,
    String alertTime,
    String memo,
    List<String> daysOfWeek
) {
}
