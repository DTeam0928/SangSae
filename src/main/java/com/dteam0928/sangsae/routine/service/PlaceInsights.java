package com.dteam0928.sangsae.routine.service;

import com.dteam0928.sangsae.routine.dto.RoutineCategoryDto;
import com.dteam0928.sangsae.routine.repository.PlaceRoutineRow;
import com.dteam0928.sangsae.routine.repository.PlaceUsage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 장소 카드에 보여줄 계산값 (태그, 시간대 문구, 적합도). DB 와 무관한 순수 계산 */
final class PlaceInsights {

    private PlaceInsights() {
    }

    /**
     * 장소 태그 상위 limit 개.
     * 그 장소 루틴들의 자유 태그 중 많이 쓰인 순 (같으면 가나다순).
     * 자유 태그가 하나도 없으면 카테고리 이름으로 대신한다 (같으면 칩 순서).
     */
    static List<String> topTags(List<PlaceRoutineRow> rows, Map<String, RoutineCategoryDto> categories, int limit) {
        Map<String, Integer> tagCounts = new HashMap<>();
        for (PlaceRoutineRow r : rows) {
            for (String tag : r.tags()) tagCounts.merge(tag, 1, Integer::sum);
        }
        if (!tagCounts.isEmpty()) {
            return tagCounts.entrySet().stream()
                    .sorted((a, b) -> b.getValue().equals(a.getValue())
                            ? a.getKey().compareTo(b.getKey())
                            : Integer.compare(b.getValue(), a.getValue()))
                    .limit(limit)
                    .map(Map.Entry::getKey)
                    .toList();
        }

        Map<String, Integer> codeCounts = new HashMap<>();
        for (PlaceRoutineRow r : rows) {
            for (String code : r.categoryCodes()) codeCounts.merge(code, 1, Integer::sum);
        }
        List<String> order = new ArrayList<>(categories.keySet());   // 칩 순서
        return codeCounts.entrySet().stream()
                .filter(e -> categories.containsKey(e.getKey()))
                .sorted((a, b) -> b.getValue().equals(a.getValue())
                        ? Integer.compare(order.indexOf(a.getKey()), order.indexOf(b.getKey()))
                        : Integer.compare(b.getValue(), a.getValue()))
                .limit(limit)
                .map(e -> categories.get(e.getKey()).name())
                .toList();
    }

    /** 루틴별 적합도를 (추가 인원 + 1) 가중치로 평균. 인기 루틴일수록 크게 반영 */
    static int weightedFit(List<PlaceRoutineRow> rows) {
        long weightSum = 0;
        long scoreSum = 0;
        for (PlaceRoutineRow r : rows) {
            long w = r.adoptedCount() + 1L;
            weightSum += w;
            scoreSum += w * r.fitScore();
        }
        return weightSum == 0 ? 0 : (int) Math.round((double) scoreSum / weightSum);
    }

    /**
     * 시간대 문구.
     * <ol>
     *   <li>최근 완료 기록이 있으면: 가장 많이 몰린 연속 2시간 → "퇴근 후 19~21시 이용 급증" / "… 이용 많음"
     *       (그 시간대의 최근 기록이 surgeMinCount 이상이고, 그 전 기간의 surgeRatio 배 이상이면 "급증")</li>
     *   <li>기록이 없으면: Task 예정 시간 기준 → "퇴근 후 18~20시 루틴 집중"</li>
     *   <li>둘 다 없으면 null</li>
     * </ol>
     */
    static String timeInsight(PlaceUsage usage, Map<Integer, Integer> plannedHours,
                              double surgeRatio, int surgeMinCount) {
        if (usage != null) {
            int hour = busiestWindow(usage.recent());
            if (hour >= 0) {
                int recent = windowSum(usage.recent(), hour);
                int previous = windowSum(usage.previous(), hour);
                boolean surge = recent >= surgeMinCount && recent > previous && recent >= previous * surgeRatio;
                return label(hour) + (surge ? " 이용 급증" : " 이용 많음");
            }
        }
        int hour = busiestWindow(plannedHours);
        return hour < 0 ? null : label(hour) + " 루틴 집중";
    }

    /**
     * 합계가 가장 큰 연속 2시간의 시작 시각. 기록이 없으면 -1.
     * 시작 시각에 기록이 있는 구간만 본다 (8시에만 기록이 있을 때 "7~9시"가 아니라 "8~10시"가 되도록).
     */
    private static int busiestWindow(Map<Integer, Integer> hourCounts) {
        if (hourCounts == null || hourCounts.isEmpty()) return -1;
        int bestHour = -1;
        int bestSum = 0;
        for (int h = 0; h <= 22; h++) {
            if (hourCounts.getOrDefault(h, 0) == 0) continue;
            int sum = windowSum(hourCounts, h);
            if (sum > bestSum) {
                bestSum = sum;
                bestHour = h;
            }
        }
        return bestHour;
    }

    private static int windowSum(Map<Integer, Integer> hourCounts, int hour) {
        return hourCounts.getOrDefault(hour, 0) + hourCounts.getOrDefault(hour + 1, 0);
    }

    private static String label(int hour) {
        return timeOfDay(hour) + " " + hour + "~" + (hour + 2) + "시";
    }

    private static String timeOfDay(int hour) {
        if (hour >= 4 && hour < 6) return "새벽";
        if (hour >= 6 && hour < 9) return "아침";
        if (hour >= 9 && hour < 12) return "오전";
        if (hour >= 12 && hour < 14) return "점심";
        if (hour >= 14 && hour < 18) return "오후";
        if (hour >= 18 && hour < 22) return "퇴근 후";
        return "밤";
    }
}
