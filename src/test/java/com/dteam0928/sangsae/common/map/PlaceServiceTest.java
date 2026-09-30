package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.dteam0928.sangsae.common.util.ConfigUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceServiceTest {

    private final PlaceService placeService = new PlaceService();

    @Test
    @DisplayName("PlaceService 장소 저장 및 단건 조회 비즈니스 로직 검증")
    void testPlaceServiceOperations() {
        String dbUser = ConfigUtil.get("DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            return;
        }

        PlaceResponseDto cafeDto = PlaceResponseDto.builder()
            .kakaoPlaceId("27517522")
            .name("어니언 성수")
            .roadAddress("서울 성동구 아차산로9길 8")
            .regions("서울특별시", "성동구", "성수동2가")
            .coordinates(37.544498, 127.058319)
            .theme(SangsaeTheme.CAFE.getCode(), SangsaeTheme.CAFE.getDisplayName())
            .originalCategoryName("음식점 > 카페")
            .build();

        // 1. Get-or-Create 호출
        Long placeId = placeService.getOrCreatePlace(cafeDto);
        assertNotNull(placeId);
        assertTrue(placeId > 0);

        // 2. 재호출 시 동일 PK 반환 검증
        Long reacquiredId = placeService.getOrCreatePlace(cafeDto);
        assertEquals(placeId, reacquiredId);

        // 3. ID로 단건 조회 검증
        PlaceResponseDto fetched = placeService.getPlaceById(placeId);
        assertNotNull(fetched);
        assertEquals("어니언 성수", fetched.name());
        assertEquals("CAFE", fetched.themeCode());
        assertEquals(37.544498, fetched.latitude(), 0.0001);
        assertEquals(127.058319, fetched.longitude(), 0.0001);
    }
}
