package com.dteam0928.sangsae.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/** users (회원 기본 계정) — DB 설계서 [도메인 1] #1 */
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password_hash")
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false, length = 20)
    private AuthProvider oauthProvider = AuthProvider.LOCAL;

    @Column(name = "oauth_id", length = 100)
    private String oauthId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private UserStatus status = UserStatus.ACTIVE;

    /** DB 기본값(CURRENT_TIMESTAMP)으로 채워지므로 JPA 에서는 쓰지 않는다 */
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public User() {
    }

    /** 이메일 회원 */
    public static User local(String email, String passwordHash) {
        User u = new User();
        u.setEmail(email);
        u.setPasswordHash(passwordHash);
        u.setOauthProvider(AuthProvider.LOCAL);
        return u;
    }

    /** 소셜 회원 (email 은 null 가능 — X 는 이메일을 주지 않을 수 있음) */
    public static User social(AuthProvider provider, String oauthId, String email) {
        User u = new User();
        u.setOauthProvider(provider);
        u.setOauthId(oauthId);
        u.setEmail(email);
        return u;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public AuthProvider getOauthProvider() { return oauthProvider; }
    public void setOauthProvider(AuthProvider oauthProvider) { this.oauthProvider = oauthProvider; }

    public String getOauthId() { return oauthId; }
    public void setOauthId(String oauthId) { this.oauthId = oauthId; }

    public UserStatus getStatus() { return status; }
    public void setStatus(UserStatus status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
