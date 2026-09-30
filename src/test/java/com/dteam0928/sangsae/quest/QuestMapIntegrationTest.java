package com.dteam0928.sangsae.quest;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.service.PlaceService;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.dteam0928.sangsae.common.util.ConfigUtil;
import com.dteam0928.sangsae.quest.dto.QuestLog;
import com.dteam0928.sangsae.quest.dto.QuestTemplate;
import com.dteam0928.sangsae.quest.dto.UserQuest;
import com.dteam0928.sangsae.quest.service.QuestService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

public class QuestMapIntegrationTest {

    private final PlaceService placeService = new PlaceService();
    private final QuestService questService = new QuestService();

    @Test
    @DisplayName("퀘스트-지도 결합: 퀘스트 상세 장소 조회 및 50m 위치인증 통과/차단 검증")
    void testQuestMapIntegration() {
        String dbUser = ConfigUtil.get("DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            return;
        }

        // 1. 장소 등록 (경복궁)
        PlaceResponseDto placeDto = PlaceResponseDto.builder()
                .kakaoPlaceId("18577297")
                .name("경복궁")
                .roadAddress("서울 종로구 사직로 161")
                .regions("서울특별시", "종로구", "세종로")
                .coordinates(37.579617, 126.977041)
                .theme(SangsaeTheme.CULTURE.getCode(), SangsaeTheme.CULTURE.getDisplayName())
                .build();

        Long placeId = placeService.getOrCreatePlace(placeDto);
        assertNotNull(placeId);

        // 2. 퀘스트 템플릿에 장소 바인딩 확인
        QuestTemplate quest = new QuestTemplate(
                99L, 1L, placeId,
                "경복궁 수문장 교대의식 관람하기",
                "경복궁 광화문 앞에서 교대의식을 관람해보세요.",
                "CULTURE",
                45, 150, "LOCATION", "PUBLIC", null, 0, null
        );

        PlaceResponseDto boundPlace = questService.getPlaceForQuest(quest);
        assertNotNull(boundPlace);
        assertEquals("경복궁", boundPlace.name());
        assertEquals(37.579617, boundPlace.latitude(), 0.0001);

        // 3. 사용자 일정에 퀘스트 담기
        UserQuest userQuest = questService.addQuestToSchedule(
                1L, 10L, 100L, LocalDate.now(), LocalTime.of(14, 0)
        );
        // 테스트용 더미의 placeId를 경복궁으로 세팅
        userQuest.setPlaceId(placeId);
        userQuest.setAuthType("LOCATION");

        // 4. [검증 A: 50m 이내 정상 현장 방문] -> 약 12m 거리 (위도 37.579700, 경도 126.977100)
        double nearLat = 37.579700;
        double nearLng = 126.977100;
        QuestLog successLog = questService.completeQuest(
                userQuest.getId(), 10L, "http://proof.jpg", "경복궁 도착 완료!", nearLat, nearLng
        );

        assertNotNull(successLog, "50m 이내 현장 방문 시 퀘스트 완료 로그가 생성되어야 합니다.");
        assertEquals("COMPLETED", userQuest.getStatus());
        assertEquals("LOCATION", successLog.getAuthType());
        System.out.println("[QuestMapIntegrationTest] ✅ 50m 이내 위치 인증 성공 검증 완료 (XP: " + successLog.getEarnedXp() + ")");

        // 5. [검증 B: 50m 초과 스푸핑/원격 시도] -> 서울시청 좌표 (약 1.5km 거리)
        UserQuest anotherUserQuest = questService.addQuestToSchedule(
                2L, 10L, 100L, LocalDate.now(), LocalTime.of(16, 0)
        );
        anotherUserQuest.setPlaceId(placeId);
        anotherUserQuest.setAuthType("LOCATION");

        double farLat = 37.566500; // 서울시청 위도
        double farLng = 126.978000; // 서울시청 경도

        IllegalStateException distanceException = assertThrows(IllegalStateException.class, () -> {
            questService.completeQuest(
                    anotherUserQuest.getId(), 10L, null, "집에서 인증 시도", farLat, farLng
            );
        });

        assertTrue(distanceException.getMessage().contains("50m 이내에서만"));
        System.out.println("[QuestMapIntegrationTest] ✅ 50m 초과 원격 인증 시도 차단 완료: " + distanceException.getMessage());

        // 6. [검증 C: GPS 좌표 미제공 시 차단]
        IllegalArgumentException noGpsException = assertThrows(IllegalArgumentException.class, () -> {
            questService.completeQuest(
                    anotherUserQuest.getId(), 10L, null, "좌표 없이 인증 시도", null, null
            );
        });

        assertTrue(noGpsException.getMessage().contains("GPS 좌표가 필수"));
        System.out.println("[QuestMapIntegrationTest] ✅ GPS 좌표 미제공 차단 완료: " + noGpsException.getMessage());
    }
}
