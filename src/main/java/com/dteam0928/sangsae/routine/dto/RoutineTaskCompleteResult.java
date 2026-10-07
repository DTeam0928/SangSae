package com.dteam0928.sangsae.routine.dto;

import com.dteam0928.sangsae.user.exp.ExpRewardResult;
import java.time.LocalDate;
import java.time.LocalTime;

public record RoutineTaskCompleteResult(
    Long taskId,
    Long logId,
    LocalDate logDate,
    LocalTime completedAt,
    int earnedXp,
    ExpRewardResult reward
) {
}
