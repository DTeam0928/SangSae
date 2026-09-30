package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dao.PlaceDao;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.dteam0928.sangsae.common.util.ConfigUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class PlaceDaoTest {

    private final PlaceDao placeDao = new PlaceDao();

    @Test
    @DisplayName("장소 Get-or-Create 중복 방지 및 Spatial 공간 검색 검증")
    void testGetOrCreateAndFindNearby() throws SQLException {
        String dbUser = ConfigUtil.get("DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            return;
        }

        // 1. 신규 장소 DTO 생성 (경복궁)
        PlaceResponseDto gyeongbokgung = PlaceResponseDto.builder()
            .kakaoPlaceId("18577297")
            .name("경복궁")
            .roadAddress("서울 종로구 사직로 161")
            .regions("서울특별시", "종로구", "세종로")
            .coordinates(37.579617, 126.977041)
            .theme(SangsaeTheme.CULTURE.getCode(), SangsaeTheme.CULTURE.getDisplayName())
            .originalCategoryName("여행 > 관광,명소 > 궁궐")
            .build();

        // 2. 최초 저장 (Create) -> 신규 PK 발급
        Long firstPlaceId = placeDao.getOrCreate(gyeongbokgung);
        assertNotNull(firstPlaceId);
        assertTrue(firstPlaceId > 0);
        System.out.println("[PlaceDaoTest] 경복궁 최초 저장 완료. PK: " + firstPlaceId);

        // 3. 동일한 장소 재저장 요청 (Get) -> 신규 INSERT 없이 기존 PK 재사용
        Long secondPlaceId = placeDao.getOrCreate(gyeongbokgung);
        assertEquals(firstPlaceId, secondPlaceId, "이미 등록된 장소는 동일한 PK를 반환해야 합니다 (중복 방지).");
        System.out.println("[PlaceDaoTest] 경복궁 중복 방지 검증 통과. PK 일치: " + secondPlaceId);

        // 4. findById 단건 조회 검증
        PlaceResponseDto found = placeDao.findById(firstPlaceId);
        assertNotNull(found);
        assertEquals("경복궁", found.name());
        assertEquals("CULTURE", found.themeCode());
        assertEquals(37.579617, found.latitude(), 0.0001);
        assertEquals(126.977041, found.longitude(), 0.0001);
        System.out.println("[PlaceDaoTest] findById 단건 조회 및 공간 좌표 복원 검증 완료: " + found.name());

        // 5. 서울시청 좌표(37.5665, 126.9780) 기준 3km 반경 검색 시 경복궁(약 1.5km) 검색 검증
        List<PlaceResponseDto> nearby = placeDao.findNearby(37.5665, 126.9780, 3000);
        assertFalse(nearby.isEmpty(), "서울시청 반경 3km 내에 경복궁이 검색되어야 합니다.");
        boolean hasGyeongbokgung = nearby.stream().anyMatch(p -> "18577297".equals(p.kakaoPlaceId()));
        assertTrue(hasGyeongbokgung, "검색 결과에 경복궁이 포함되어 있어야 합니다.");
        System.out.println("[PlaceDaoTest] Spatial 반경 3km 공간 검색 성공. 검색된 장소 수: " + nearby.size());
    }
}
