package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import jakarta.persistence.EntityManager;

import java.util.List;

/** 내가 다니는 장소 (user_places) + 카카오 장소 저장 (places) */
public interface MyPlaceRepository {

    /** 등록 순. origin 이 null 이면 거리 없음 */
    List<PlaceSummary> findMyPlaces(EntityManager em, long userId, GeoPoint origin);

    int countMyPlaces(EntityManager em, long userId);

    boolean existsPlace(EntityManager em, long placeId);

    /** kakao_place_id 기준으로 있으면 기존 id, 없으면 새로 저장한 id */
    long upsertKakaoPlace(EntityManager em, String kakaoPlaceId, String name, String roadAddress,
                          String region1, String region2, String region3, GeoPoint point, String category);

    /** 이미 등록돼 있으면 아무 일도 하지 않음 */
    void addMyPlace(EntityManager em, long userId, long placeId);

    void removeMyPlace(EntityManager em, long userId, long placeId);
}
