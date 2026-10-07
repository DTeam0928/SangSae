package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.common.geo.GeoPoint;

/**
 * 루틴 카드 목록 조회 조건. 섹션(인기/최신/추천/근처)과 검색이 모두 이 조건 하나로 표현된다.
 *
 * @param userId         로그인 사용자 (찜 여부, 적합도 계산용)
 * @param origin         거리 계산 기준 위치 (필수)
 * @param radiusMeters   대표 장소가 이 반경 안인 루틴만 (null 이면 제한 없음)
 * @param keyword        제목·설명·장소명·카테고리명·자유 태그 검색어 (null 이면 제한 없음)
 * @param categoryCode   카테고리 칩 필터 (null 이면 전체)
 * @param placeCategory  대표 장소 유형 필터 (GYM, CAFE … null 이면 전체)
 * @param excludeAdopted true 면 내가 이미 추가한 루틴 제외
 * @param trendingDays   recentAdoptedCount 집계 기간(일)
 */
public record CardQuery(long userId, GeoPoint origin, Integer radiusMeters, String keyword, String categoryCode,
                        String placeCategory, boolean excludeAdopted, int trendingDays, CardSort sort,
                        int offset, int limit) {

    public static CardQuery of(long userId, GeoPoint origin, int trendingDays, CardSort sort, int limit) {
        return new CardQuery(userId, origin, null, null, null, null, false, trendingDays, sort, 0, limit);
    }

    public CardQuery withRadius(Integer meters) {
        return new CardQuery(userId, origin, meters, keyword, categoryCode, placeCategory, excludeAdopted,
                trendingDays, sort, offset, limit);
    }

    public CardQuery withKeyword(String kw) {
        return new CardQuery(userId, origin, radiusMeters, kw, categoryCode, placeCategory, excludeAdopted,
                trendingDays, sort, offset, limit);
    }

    public CardQuery withCategory(String code) {
        return new CardQuery(userId, origin, radiusMeters, keyword, code, placeCategory, excludeAdopted,
                trendingDays, sort, offset, limit);
    }

    public CardQuery withPlaceCategory(String code) {
        return new CardQuery(userId, origin, radiusMeters, keyword, categoryCode, code, excludeAdopted,
                trendingDays, sort, offset, limit);
    }

    public CardQuery excludingAdopted() {
        return new CardQuery(userId, origin, radiusMeters, keyword, categoryCode, placeCategory, true,
                trendingDays, sort, offset, limit);
    }

    public CardQuery page(int newOffset, int newLimit) {
        return new CardQuery(userId, origin, radiusMeters, keyword, categoryCode, placeCategory, excludeAdopted,
                trendingDays, sort, newOffset, newLimit);
    }
}
