package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/** 장소 상세 모달 / 내 장소 셀렉트 변경 결과 */
public record PlaceDetail(NearbySpot spot, List<PlaceRoutine> routines) {
}
