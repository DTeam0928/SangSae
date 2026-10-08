package com.dteam0928.sangsae.routine.repository.jpa;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.repository.MyPlaceRepository;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.List;

import static com.dteam0928.sangsae.routine.repository.jpa.NativeRows.*;

public class JpaMyPlaceRepository implements MyPlaceRepository {

    @Override
    public List<PlaceSummary> findMyPlaces(EntityManager em, long userId, GeoPoint origin) {
        String dist = origin == null ? "NULL" : "ST_Distance(p.coordinates, ST_GeomFromText(?2, 4326))";
        Query q = em.createNativeQuery("SELECT " + PLACE_COLUMNS + ", " + dist
                        + " FROM user_places up JOIN places p ON p.id = up.place_id" + JOIN_PLACE_CATEGORY
                        + " WHERE up.user_id = ?1 ORDER BY up.created_at, p.id")
                .setParameter(1, userId);
        if (origin != null) q.setParameter(2, origin.toWkt());
        return rows(q).stream().map(r -> place(r, 0)).toList();
    }

    @Override
    public int countMyPlaces(EntityManager em, long userId) {
        return i(em.createNativeQuery("SELECT COUNT(*) FROM user_places WHERE user_id = ?1")
                .setParameter(1, userId).getSingleResult());
    }

    @Override
    public boolean existsPlace(EntityManager em, long placeId) {
        return i(em.createNativeQuery("SELECT COUNT(*) FROM places WHERE id = ?1")
                .setParameter(1, placeId).getSingleResult()) > 0;
    }

    @Override
    public long upsertKakaoPlace(EntityManager em, String kakaoPlaceId, String name, String roadAddress,
                                 String region1, String region2, String region3, GeoPoint point, String category) {
        // 이미 있는 카카오 장소면 기존 행을 그대로 두고 그 id 를 LAST_INSERT_ID 로 받는다
        em.createNativeQuery("INSERT INTO places (kakao_place_id, name, road_address, region_1depth_name,"
                        + " region_2depth_name, region_3depth_name, coordinates, category)"
                        + " VALUES (?1, ?2, ?3, ?4, ?5, ?6, " + GeoPoint.sqlFromWkt(7) + ", ?8)"
                        + " ON DUPLICATE KEY UPDATE id = LAST_INSERT_ID(id)")
                .setParameter(1, kakaoPlaceId)
                .setParameter(2, name)
                .setParameter(3, roadAddress)
                .setParameter(4, region1)
                .setParameter(5, region2)
                .setParameter(6, region3)
                .setParameter(7, point.toWkt())
                .setParameter(8, category)
                .executeUpdate();
        return l(em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult());
    }

    @Override
    public void addMyPlace(EntityManager em, long userId, long placeId) {
        em.createNativeQuery("INSERT IGNORE INTO user_places (user_id, place_id) VALUES (?1, ?2)")
                .setParameter(1, userId).setParameter(2, placeId).executeUpdate();
    }

    @Override
    public void removeMyPlace(EntityManager em, long userId, long placeId) {
        em.createNativeQuery("DELETE FROM user_places WHERE user_id = ?1 AND place_id = ?2")
                .setParameter(1, userId).setParameter(2, placeId).executeUpdate();
    }
}
