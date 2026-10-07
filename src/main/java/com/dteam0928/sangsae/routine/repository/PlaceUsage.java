package com.dteam0928.sangsae.routine.repository;

import java.util.HashMap;
import java.util.Map;

/**
 * 장소 한 곳의 실제 이용(Task 완료) 기록을 시간대별로 센 것.
 *
 * @param recent   최근 N일 : 시(0~23) → 완료 수
 * @param previous 그 전 N일 : 시(0~23) → 완료 수 ("급증" 비교용)
 */
public record PlaceUsage(Map<Integer, Integer> recent, Map<Integer, Integer> previous) {

    public static PlaceUsage empty() {
        return new PlaceUsage(new HashMap<>(), new HashMap<>());
    }
}
