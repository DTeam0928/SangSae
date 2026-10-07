package com.dteam0928.sangsae.routine.dto;

import java.time.LocalDate;

public record RoutineAdoptRequest(
    Long routineTemplateId,
    LocalDate startDate,
    LocalDate endDate
) {
}
