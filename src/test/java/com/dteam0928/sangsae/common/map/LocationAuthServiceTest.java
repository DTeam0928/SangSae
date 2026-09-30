package com.dteam0928.sangsae.common.map;

import com.dteam0928.sangsae.common.map.dto.LocationAuthResult;
import com.dteam0928.sangsae.common.map.dto.LocationAuthStatus;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class LocationAuthServiceTest {

    private LocationAuthService locationAuthService;

    // 서울숲 군마상 좌표 기준 (위도 37.544387, 경도 127.037442)
    private PlaceResponseDto seoulForestPlace;

    @BeforeEach
    void setUp() {
        locationAuthService = new LocationAuthService();

        seoulForestPlace = PlaceResponseDto.builder()
                .kakaoPlaceId("1234567")
                .name("서울숲 군마상")
                .roadAddress("서울 성동구 뚝섬로 273")
                .regions("서울특별시", "성동구", "성수동1가")
                .coordinates(37.544387, 127.037442)
                .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                .originalCategoryName("공원")
                .build();
    }

    @Test
    @DisplayName("50m 이내 현장 방문 시 위치 인증 성공 판정")
    void testVerifyLocationSuccessWithinRadius() {
        // 서울숲 군마상으로부터 약 20m 떨어진 위치 (위도 0.00018도 차이)
        double nearLat = 37.544567;
        double nearLng = 127.037442;

        LocationAuthResult result = locationAuthService.verifyLocation(seoulForestPlace, nearLat, nearLng, 50);

        assertTrue(result.isSuccess());
        assertEquals(LocationAuthStatus.SUCCESS, result.status());
        assertTrue(result.distanceInMeters() <= 50);
        assertTrue(result.distanceInMeters() >= 0);
        assertTrue(result.message().contains("성공"));
        assertEquals("서울숲 군마상", result.place().name());
    }

    @Test
    @DisplayName("50m 허용 반경 초과 시 위치 인증 실패 판정")
    void testVerifyLocationFailExceededRadius() {
        // 서울숲 군마상으로부터 약 500m 이상 떨어진 위치 (성수역 근처)
        double farLat = 37.544581;
        double farLng = 127.056000;

        LocationAuthResult result = locationAuthService.verifyLocation(seoulForestPlace, farLat, farLng, 50);

        assertFalse(result.isSuccess());
        assertEquals(LocationAuthStatus.DISTANCE_EXCEEDED, result.status());
        assertTrue(result.distanceInMeters() > 50);
        assertTrue(result.message().contains("50m 이내"));
    }

    @Test
    @DisplayName("순수 위경도 좌표 간 거리 검증 (장소 엔티티 없는 경우)")
    void testVerifyCoordinates() {
        double targetLat = 37.544387;
        double targetLng = 127.037442;

        // 약 15m 거리
        double userLat = 37.544520;
        double userLng = 127.037442;

        LocationAuthResult successResult = locationAuthService.verifyCoordinates(targetLat, targetLng, userLat, userLng, 50);
        assertTrue(successResult.isSuccess());
        assertEquals(LocationAuthStatus.SUCCESS, successResult.status());

        // 300m 거리
        double distantLat = 37.547087;
        LocationAuthResult failResult = locationAuthService.verifyCoordinates(targetLat, targetLng, distantLat, userLng, 50);
        assertFalse(failResult.isSuccess());
        assertEquals(LocationAuthStatus.DISTANCE_EXCEEDED, failResult.status());
    }

    @Test
    @DisplayName("잘못되거나 미수신된 GPS 좌표 (null, 0.0, 범위 초과) 차단 검증")
    void testInvalidCoordinates() {
        LocationAuthResult nullResult = locationAuthService.verifyLocation(seoulForestPlace, null, null, 50);
        assertFalse(nullResult.isSuccess());
        assertEquals(LocationAuthStatus.INVALID_COORDINATES, nullResult.status());

        LocationAuthResult zeroResult = locationAuthService.verifyLocation(seoulForestPlace, 0.0, 0.0, 50);
        assertFalse(zeroResult.isSuccess());
        assertEquals(LocationAuthStatus.INVALID_COORDINATES, zeroResult.status());

        LocationAuthResult outOfRangeResult = locationAuthService.verifyLocation(seoulForestPlace, 100.0, 200.0, 50);
        assertFalse(outOfRangeResult.isSuccess());
        assertEquals(LocationAuthStatus.INVALID_COORDINATES, outOfRangeResult.status());
    }

    @Test
    @DisplayName("validateOrThrow 호출 시 반경 초과 시 IllegalStateException 발생")
    void testValidateOrThrow() {
        // 성수역 근처 좌표 (서울숲 군마상에서 1km 이상)
        double farLat = 37.544581;
        double farLng = 127.056000;

        assertThrows(IllegalStateException.class, () -> {
            LocationAuthResult result = locationAuthService.verifyLocation(seoulForestPlace, farLat, farLng, 50);
            if (!result.isSuccess()) {
                throw new IllegalStateException(result.message());
            }
        });
    }
}
