package com.dteam0928.sangsae.routine.dto;

/**
 * 장소 요약.
 *
 * @param category       장소 유형 코드 (GYM, CAFE …)
 * @param categoryName   장소 유형 이름 (헬스장, 카페 …). place_categories 에 없는 코드면 null
 * @param distanceMeters 기준 위치에서의 거리(미터, 반올림). 기준 위치가 없으면 null
 */
public record PlaceSummary(long id, String name, String category, String categoryName, String roadAddress,
                           double latitude, double longitude, Integer distanceMeters) {
}
