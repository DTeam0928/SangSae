package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class MapComponentTest {

    @Test
    @DisplayName("상새 8대 테마 매핑 테스트")
    void testThemeMapping() {
        assertEquals(SangsaeTheme.CAFE, SangsaeTheme.fromKakao("CE7", "음식점 > 카페", "스타벅스 광화문점"));
        assertEquals(SangsaeTheme.FOOD, SangsaeTheme.fromKakao("FD6", "음식점 > 한식", "이문설농탕"));
        assertEquals(SangsaeTheme.CULTURE, SangsaeTheme.fromKakao("CT1", "문화,예술 > 박물관", "국립현대미술관"));
        assertEquals(SangsaeTheme.WALK, SangsaeTheme.fromKakao("", "여행 > 공원", "여의도 한강공원 산책로"));
        assertEquals(SangsaeTheme.ACTIVITY, SangsaeTheme.fromKakao("", "스포츠,레저 > 헬스클럽", "더클라임 클라이밍짐"));
        assertEquals(SangsaeTheme.STUDY, SangsaeTheme.fromKakao("AC5", "교육,학문 > 도서관", "정독도서관"));
        assertEquals(SangsaeTheme.NATURE, SangsaeTheme.fromKakao("", "여행 > 명소", "광안리 해수욕장"));
        assertEquals(SangsaeTheme.PHOTO, SangsaeTheme.fromKakao("AT4", "여행 > 관광명소", "북촌한옥마을 포토스팟"));
    }

    @Test
    @DisplayName("Haversine 거리 계산 테스트 (서울시청 ↔ 경복궁)")
    void testDistanceCalculation() {
        // 서울시청
        double lat1 = 37.5665;
        double lon1 = 126.9780;
        // 경복궁
        double lat2 = 37.5796;
        double lon2 = 126.9770;

        int distance = PlaceSearchService.calculateDistanceInMeters(lat1, lon1, lat2, lon2);
        // 서울시청과 경복궁은 대략 1.4~1.6km (1400~1600m)
        assertTrue(distance > 1300 && distance < 1700, "거리는 약 1.4~1.6km 사이여야 함. 실제: " + distance);
    }

    @Test
    @DisplayName("PlaceResponseDto 빌더 및 불변 Record 생성 테스트")
    void testPlaceResponseDto() {
        PlaceResponseDto dto = PlaceResponseDto.builder()
                .kakaoPlaceId("12345")
                .name("광화문 광장")
                .roadAddress("서울 종로구 세종대로 172")
                .address("서울 종로구 세종로 1-68")
                .regions("서울특별시", "종로구", "세종로")
                .coordinates(37.5714, 126.9768)
                .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                .originalCategoryName("여행 > 명소 > 광장")
                .distanceInMeters(500)
                .phone("02-120")
                .placeUrl("http://place.map.kakao.com/12345")
                .build();

        assertEquals("12345", dto.kakaoPlaceId());
        assertEquals("광화문 광장", dto.name());
        assertEquals("WALK", dto.themeCode());
        assertEquals("#산책/조깅", dto.themeName());
        assertEquals(500, dto.distanceInMeters());
    }
}
