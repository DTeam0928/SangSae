package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.common.config.AppConfig;

/**
 * 루틴 탐색 설정값 (application.properties 의 routine.*). 숫자를 바꿀 때 코드 수정 없이 설정만 고치면 된다.
 *
 * @param defaultLatitude    GPS·저장 위치가 모두 없을 때 기준 위치 (기본: 서울 시청)
 * @param nearbyRadiusMeters "내 주변" 반경
 * @param trendingDays       "지금 뜨는" 집계 기간(일)
 * @param sectionSize        섹션별 카드 수
 * @param mapPlaceLimit      지도 핀 최대 수
 * @param pageSize           검색 결과 한 페이지 크기
 * @param myPlaceLimit       내가 다니는 장소 최대 등록 수
 * @param usageDays          장소 이용 통계 기간(일). 최근 N일과 그 전 N일을 비교한다
 * @param surgeRatio         "이용 급증" 기준: 최근 N일 ≥ 그 전 N일 × 이 배수
 * @param surgeMinCount      "이용 급증" 최소 건수 (기록이 너무 적을 때 급증으로 보지 않음)
 */
public record RoutineSettings(double defaultLatitude, double defaultLongitude, int nearbyRadiusMeters,
                              int trendingDays, int sectionSize, int mapPlaceLimit, int pageSize,
                              int myPlaceLimit, int usageDays, double surgeRatio, int surgeMinCount) {

    public static RoutineSettings from(AppConfig config) {
        return new RoutineSettings(
                Double.parseDouble(config.get("routine.default-latitude", "37.5665")),
                Double.parseDouble(config.get("routine.default-longitude", "126.9780")),
                config.getInt("routine.nearby-radius-meters", 3000),
                config.getInt("routine.trending-days", 7),
                config.getInt("routine.section-size", 6),
                config.getInt("routine.map-place-limit", 20),
                config.getInt("routine.page-size", 12),
                config.getInt("routine.my-place-limit", 10),
                config.getInt("routine.usage-days", 7),
                Double.parseDouble(config.get("routine.surge-ratio", "1.5")),
                config.getInt("routine.surge-min-count", 3));
    }
}
