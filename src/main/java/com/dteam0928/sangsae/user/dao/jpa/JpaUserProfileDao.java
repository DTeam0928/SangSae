package com.dteam0928.sangsae.user.dao.jpa;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.user.dao.UserProfileDao;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class JpaUserProfileDao implements UserProfileDao {

    @Override
    public void insert(EntityManager em, UserProfile profile) {
        em.persist(profile);
        em.flush();   // 닉네임 UNIQUE 위반을 이 시점에 드러냄
        if (profile.getLastKnownLocation() != null) {
            updateLocation(em, profile.getUserId(), profile.isLocationPermitted(), profile.getLastKnownLocation());
        }
    }

    @Override
    public Optional<UserProfile> findByUserId(EntityManager em, long userId) {
        UserProfile profile = em.find(UserProfile.class, userId);
        if (profile == null) return Optional.empty();

        List<?> rows = em.createNativeQuery(
                        "SELECT ST_Latitude(last_known_location), ST_Longitude(last_known_location) "
                                + "FROM user_profiles WHERE user_id = ?1 AND last_known_location IS NOT NULL")
                .setParameter(1, userId)
                .getResultList();
        if (!rows.isEmpty()) {
            Object[] row = (Object[]) rows.get(0);
            profile.setLastKnownLocation(GeoPoint.of(((Number) row[0]).doubleValue(), ((Number) row[1]).doubleValue()));
        }
        return Optional.of(profile);
    }

    @Override
    public boolean existsByNickname(EntityManager em, String nickname) {
        return em.createQuery("SELECT COUNT(p) FROM UserProfile p WHERE p.nickname = :nickname", Long.class)
                .setParameter("nickname", nickname)
                .getSingleResult() > 0;
    }

    @Override
    public void updateNickname(EntityManager em, long userId, String nickname) {
        UserProfile profile = em.find(UserProfile.class, userId);
        if (profile == null) throw new IllegalStateException("프로필이 없습니다: " + userId);
        profile.setNickname(nickname);   // 변경 감지(dirty checking)로 UPDATE
        em.flush();
    }

    @Override
    public void updateLocation(EntityManager em, long userId, boolean permitted, GeoPoint location) {
        if (location == null) {
            em.createNativeQuery("UPDATE user_profiles SET is_location_permitted = ?1, last_known_location = NULL "
                            + "WHERE user_id = ?2")
                    .setParameter(1, permitted)
                    .setParameter(2, userId)
                    .executeUpdate();
        } else {
            em.createNativeQuery("UPDATE user_profiles SET is_location_permitted = ?1, last_known_location = "
                            + GeoPoint.sqlFromWkt(2) + " WHERE user_id = ?3")
                    .setParameter(1, permitted)
                    .setParameter(2, location.toWkt())
                    .setParameter(3, userId)
                    .executeUpdate();
        }
    }
}
