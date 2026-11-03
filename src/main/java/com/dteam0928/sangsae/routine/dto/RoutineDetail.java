package com.dteam0928.sangsae.routine.dto;

import java.util.List;

/**
 * 루틴 상세 모달.
 *
 * @param performerCount 지금 이 루틴을 활성 상태로 실천 중인 인원
 * @param alreadyAdopted 내가 이미 내 루틴에 추가했는지 (true 면 [내 루틴에 추가] 비활성)
 */
public record RoutineDetail(RoutineCard routine, List<RoutineTaskDto> tasks, int performerCount,
                            boolean alreadyAdopted) {
}
