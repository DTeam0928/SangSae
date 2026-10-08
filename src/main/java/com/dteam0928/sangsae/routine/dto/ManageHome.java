package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 관리 탭 첫 로딩.
 *
 * @param routines   활성 루틴 셀렉트 (만든 순). 비어 있으면 "새 루틴 만들기" 안내
 * @param categories Task·공개 설정 카테고리 선택지
 * @param myPlaces   Task 장소 선택지 (내가 다니는 장소)
 */
public record ManageHome(List<MyRoutine> routines, List<RoutineCategoryDto> categories, List<PlaceSummary> myPlaces) {
}
