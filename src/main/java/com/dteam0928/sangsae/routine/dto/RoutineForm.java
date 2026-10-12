package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 루틴 생성·기본 정보 수정 입력값 (검증은 서비스에서).
 *
 * @param visibility PUBLIC / PRIVATE (없으면 PRIVATE)
 * @param categories 공개할 때 필수 (routine_categories.code)
 */
public record RoutineForm(String title, String startDate, String endDate, String visibility,
                          String description, List<String> categories, List<String> tags,
                          Long representativePlaceId) {
}
