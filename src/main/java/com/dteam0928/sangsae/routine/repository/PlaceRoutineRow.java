package com.dteam0928.sangsae.routine.repository;

import java.util.List;

/**
 * 장소-루틴 집계 한 줄. 서비스가 장소별 태그·적합도·인기 루틴을 계산할 때 쓴다.
 * 루틴이 장소에 속하는 기준: 대표 장소이거나, Task 중 하나라도 그 장소인 경우.
 */
public record PlaceRoutineRow(long placeId, long templateId, String title, int adoptedCount, int performerCount,
                              List<String> categoryCodes, List<String> tags, int tagTotal, int tagMatch) {

    /** 내 관심사 적합도 0~100 = 관심사와 연결된 카테고리 수 / 전체 카테고리 수 */
    public int fitScore() {
        return tagTotal == 0 ? 0 : (int) Math.round(100.0 * tagMatch / tagTotal);
    }
}
