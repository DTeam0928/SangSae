package com.dteam0928.sangsae.routine.repository;

import java.time.LocalDate;

/** 완료 기록 (routine_task_logs, is_success = 1). completedAt = "HH:mm" */
public record LogRow(long taskId, LocalDate date, String completedAt) {
}
