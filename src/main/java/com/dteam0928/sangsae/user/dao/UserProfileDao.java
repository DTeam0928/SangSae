package com.dteam0928.sangsae.user.dao;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.user.model.UserProfile;
import jakarta.persistence.EntityManager;

import java.util.Optional;

/** user_profiles 접근 */
public interface UserProfileDao {

    /** 제약 이름 (DB 설계서 4. 인덱스 명세) */
    String UK_NICKNAME = "uk_profiles_nickname";

    void insert(EntityManager em, UserProfile profile);

    /** 좌표(last_known_location)까지 채워서 반환 */
    Optional<UserProfile> findByUserId(EntityManager em, long userId);

    boolean existsByNickname(EntityManager em, String nickname);

    void updateNickname(EntityManager em, long userId, String nickname);

    /** location 이 null 이면 좌표를 비운다 */
    void updateLocation(EntityManager em, long userId, boolean permitted, GeoPoint location);
}
