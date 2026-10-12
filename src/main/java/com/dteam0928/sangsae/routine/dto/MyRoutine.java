package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 내 루틴 (관리 탭 셀렉트 + 기본 정보 폼).
 *
 * 날짜는 "yyyy-MM-dd" 문자열 (공용 JsonUtil 이 java.time 을 변환하지 못해서).
 *
 * @param endDate            없으면 null (종료일 없음)
 * @param visibility         PUBLIC 이면 탐색 탭에 공개 중. 다른 사람 루틴을 추가한 경우는 항상 PRIVATE
 * @param adopted            탐색 탭에서 다른 사람(또는 기본 제공) 루틴을 추가한 것. 공개 설정을 바꿀 수 없다
 * @param description        공개용 설명 (직접 만든 루틴만)
 * @param categories         공개용 카테고리
 * @param tags               공개용 자유 태그
 * @param representativePlace 공개용 대표 장소 (없으면 null)
 * @param taskCount          Task 수
 */
public record MyRoutine(long id, String title, String startDate, String endDate,
                        String visibility, boolean adopted, String description,
                        List<RoutineCategoryDto> categories, List<String> tags, PlaceSummary representativePlace,
                        int taskCount) {
}
