package com.dteam0928.sangsae.routine.dto;

import java.time.LocalDate;

public record UserRoutineCreateRequest(
    String title,
    LocalDate startDate,
    LocalDate endDate
) {
}
