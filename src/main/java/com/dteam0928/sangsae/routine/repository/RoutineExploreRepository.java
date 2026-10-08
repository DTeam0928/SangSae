package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.dto.PlaceCategoryDto;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import com.dteam0928.sangsae.routine.dto.RoutineCard;
import com.dteam0928.sangsae.routine.dto.RoutineCategoryDto;
import com.dteam0928.sangsae.routine.dto.RoutineTaskDto;
import jakarta.persistence.EntityManager;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** 루틴 탐색: 조회 + 찜 (내 루틴에 추가는 RoutineService 담당) */
public interface RoutineExploreRepository {

    /** 카테고리 칩 (sort_order 순) */
    List<RoutineCategoryDto> findCategories(EntityManager em);

    /** 장소 유형 이름표 (sort_order 순) */
    List<PlaceCategoryDto> findPlaceCategories(EntityManager em);

    /** 공개 루틴 카드 목록 (섹션·검색 공용) */
    List<RoutineCard> findCards(EntityManager em, CardQuery query);

    /** 공개 루틴 카드 1장 */
    Optional<RoutineCard> findCard(EntityManager em, long userId, long templateId, GeoPoint origin, int trendingDays);

    List<RoutineTaskDto> findTasks(EntityManager em, long templateId);

    /** 지금 이 루틴을 활성 상태로 실천 중인 인원 */
    int countPerformers(EntityManager em, long templateId);

    /** 내가 이 템플릿으로 만든 활성 루틴이 있는지 */
    boolean hasActiveFork(EntityManager em, long userId, long templateId);

    /** 반경 안에서 공개 루틴이 연결된 장소 (가까운 순). placeCategory 가 null 이면 전체 유형 */
    List<PlaceSummary> findPlacesWithRoutines(EntityManager em, GeoPoint origin, int radiusMeters,
                                              String placeCategory, int limit);

    Optional<PlaceSummary> findPlace(EntityManager em, long placeId, GeoPoint origin);

    /** 장소별 공개 루틴 집계 */
    List<PlaceRoutineRow> findRoutinesByPlaces(EntityManager em, long userId, Collection<Long> placeIds);

    /** 장소별 Task 예정 시간 분포: placeId → (시(0~23) → Task 수) */
    Map<Long, Map<Integer, Integer>> findTaskHoursByPlaces(EntityManager em, Collection<Long> placeIds);

    /**
     * 장소별 실제 이용(성공한 Task 완료 기록) 시간대 분포. routine_task_logs 기준 (읽기 전용).
     * 최근 days 일과 그 전 days 일을 따로 센다.
     */
    Map<Long, PlaceUsage> findUsageByPlaces(EntityManager em, Collection<Long> placeIds, int days);

    // ---- 쓰기 ----

    boolean existsPublicTemplate(EntityManager em, long templateId);

    void addBookmark(EntityManager em, long userId, long templateId);

    void removeBookmark(EntityManager em, long userId, long templateId);

}
