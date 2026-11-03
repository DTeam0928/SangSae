package com.dteam0928.sangsae.map;

import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MapDomainTest {

    @Test
    @DisplayName("지도: 두 위경도 좌표 간 하버사인 구면 거리 계산 검증")
    void testCalculateDistanceInMeters() {
        // 서울시청 (37.5665, 126.9780) -> 광화문광장 (37.5714, 126.9769)
        // 약 550m ~ 600m 사이
        int distance = PlaceSearchService.calculateDistanceInMeters(
                37.5665, 126.9780,
                37.5714, 126.9769
        );

        assertTrue(distance >= 500 && distance <= 650, "서울시청과 광화문 거리는 약 550m 내외여야 합니다. 실제: " + distance);
    }

    @Test
    @DisplayName("지도: 동일 좌표 간 거리는 0m")
    void testSameCoordinateDistance() {
        int distance = PlaceSearchService.calculateDistanceInMeters(
                37.5665, 126.9780,
                37.5665, 126.9780
        );
        assertEquals(0, distance);
    }

    @Test
    @DisplayName("테마: 카카오 카테고리 코드 및 상새 테마 매핑 검증")
    void testThemeMapping() {
        assertEquals("#산책/조깅", SangsaeTheme.WALK.getDisplayName());
        assertEquals("#문화/예술", SangsaeTheme.CULTURE.getDisplayName());
        assertEquals("#맛집", SangsaeTheme.FOOD.getDisplayName());
        assertEquals("#카페", SangsaeTheme.CAFE.getDisplayName());
    }
}
