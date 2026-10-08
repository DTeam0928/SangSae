package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * "지금 내 주변 루틴 스팟" 카드 / 지도 핀.
 *
 * @param tags        그 장소 루틴들의 자유 태그 상위 3개 (자유 태그가 없으면 카테고리 이름)
 * @param timeInsight 실제 완료 기록 기준 "퇴근 후 19~21시 이용 급증" / "… 이용 많음",
 *                    기록이 없으면 Task 예정 시간 기준 "… 루틴 집중", 둘 다 없으면 null
 * @param fitScore    내 관심사 적합도 0~100 (인기 루틴일수록 크게 반영한 평균)
 */
public record NearbySpot(PlaceSummary place, int routineCount, List<String> tags, String timeInsight,
                         int fitScore) {
}
