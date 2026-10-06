package com.dteam0928.sangsae.quest;

import com.dteam0928.sangsae.common.map.dto.LocationAuthResult;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.LocationAuthService;
import com.dteam0928.sangsae.common.map.service.PlaceSearchService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.dteam0928.sangsae.quest.dto.QuestDetailResponseDto;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class QuestMapIntegrationTest {

    private final LocationAuthService locationAuthService = new LocationAuthService();

    @Test
    @DisplayName("퀘스트 위치 인증: 50m 이내 정상 현장 방문 시 성공 판정")
    void verifyLocation_within50m_success() {
        // 목표 장소: 경복궁 광화문 (위도: 37.5759, 경도: 126.9768)
        double targetLat = 37.575900;
        double targetLng = 126.976800;

        // 사용자 현재 위치: 약 15m 거리 (위도: 37.576000, 경도: 126.976900)
        double userLat = 37.576000;
        double userLng = 126.976900;

        LocationAuthResult result = locationAuthService.verifyCoordinates(targetLat, targetLng, userLat, userLng, 50);

        assertTrue(result.isSuccess(), "50m 이내 좌표는 인증 성공해야 합니다.");
        assertTrue(result.distanceInMeters() <= 50, "계산된 거리는 50m 이하이어야 합니다.");
    }

    @Test
    @DisplayName("퀘스트 위치 인증: 50m 초과 원격 인증 시도 시 차단 및 초과 거리 반환")
    void verifyLocation_over50m_fails() {
        // 목표 장소: 경복궁 (위도: 37.5759, 경도: 126.9768)
        double targetLat = 37.575900;
        double targetLng = 126.976800;

        // 사용자 위치: 서울시청 부근 (약 1.3km 떨어짐)
        double userLat = 37.566500;
        double userLng = 126.978000;

        LocationAuthResult result = locationAuthService.verifyCoordinates(targetLat, targetLng, userLat, userLng, 50);

        assertFalse(result.isSuccess(), "50m를 초과한 좌표는 인증 실패해야 합니다.");
        assertTrue(result.distanceInMeters() > 50, "계산된 거리가 50m보다 커야 합니다.");
        assertTrue(result.message().contains("50m 이내"), "에러 메시지에 허용 반경 안내가 포함되어야 합니다.");
    }

    @Test
    @DisplayName("퀘스트 상세 응답 DTO: 퀘스트와 장소 정보가 정상 결합되는지 검증")
    void questDetailResponseDto_combinesTemplateAndPlace() {
        PlaceResponseDto place = PlaceResponseDto.builder()
                .placeId(10L)
                .name("경복궁")
                .roadAddress("서울 종로구 사직로 161")
                .coordinates(37.5759, 126.9768)
                .theme(SangsaeTheme.CULTURE.getCode(), SangsaeTheme.CULTURE.getDisplayName())
                .build();

        QuestTemplate template = new QuestTemplate(
                1L, 100L, 10L, "경복궁 탐방", "경복궁을 산책하며 사진 찍기",
                "CULTURE", 60, 200, "LOCATION", "PUBLIC", null, 5, 12, LocalDateTime.now()
        );

        QuestDetailResponseDto dto = QuestDetailResponseDto.of(template, place, true);

        assertEquals(1L, dto.id());
        assertEquals("경복궁 탐방", dto.title());
        assertEquals(10L, dto.placeId());
        assertNotNull(dto.place());
        assertEquals("경복궁", dto.place().name());
        assertEquals(37.5759, dto.place().latitude());
        assertTrue(dto.bookmarked());
    }
}
