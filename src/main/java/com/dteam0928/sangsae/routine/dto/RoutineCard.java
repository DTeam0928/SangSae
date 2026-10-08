package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 루틴 카드 1장.
 *
 * @param categories         카테고리 칩 기준 태그 (운동, 직장인 …)
 * @param tags               자유 태그 (웨이트, 퇴근 운동, 초보 …)
 * @param place              대표 장소 (없으면 null)
 * @param adoptedCount       🔥 누적 "내 루틴에 추가" 인원
 * @param recentAdoptedCount 최근 N일(routine.trending-days) 동안 추가된 수
 * @param fitScore           내 관심사 적합도 0~100
 * @param createdAt          ISO 형식 문자열 (yyyy-MM-ddTHH:mm:ss)
 */
public record RoutineCard(long id, String title, String description, List<RoutineCategoryDto> categories,
                          List<String> tags, PlaceSummary place, int taskCount, int adoptedCount,
                          int recentAdoptedCount, int fitScore, boolean bookmarked, String createdAt) {
}
