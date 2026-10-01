package com.dteam0928.sangsae.user.model;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import java.time.LocalDateTime;

/**
 * user_profiles (회원 프로필 & 위치 권한) — DB 설계서 [도메인 1] #2
 * users 와 1:1, PK = users.id. last_known_location(POINT)은 JPA 매핑 대신 DAO 의 native 쿼리로 처리한다.
 */
@Entity
@Table(name = "user_profiles")
public class UserProfile {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", nullable = false, length = 30)
    private String nickname;

    @Column(name = "profile_image_url", length = 500)
    private String profileImageUrl;

    @Column(name = "is_location_permitted", nullable = false)
    private boolean locationPermitted;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    /** POINT 컬럼 — DAO 가 조회 시 채워 준다 */
    @Transient
    private GeoPoint lastKnownLocation;

    public UserProfile() {
    }

    public UserProfile(Long userId, String nickname) {
        this.userId = userId;
        this.nickname = nickname;
    }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public String getProfileImageUrl() { return profileImageUrl; }
    public void setProfileImageUrl(String profileImageUrl) { this.profileImageUrl = profileImageUrl; }

    public boolean isLocationPermitted() { return locationPermitted; }
    public void setLocationPermitted(boolean locationPermitted) { this.locationPermitted = locationPermitted; }

    public GeoPoint getLastKnownLocation() { return lastKnownLocation; }
    public void setLastKnownLocation(GeoPoint lastKnownLocation) { this.lastKnownLocation = lastKnownLocation; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
