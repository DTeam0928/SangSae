package com.dteam0928.sangsae.user.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;

/** user_interests (회원-관심사 매핑, 복합 PK) — DB 설계서 [도메인 1] #4 */
@Entity
@Table(name = "user_interests")
@IdClass(UserInterest.Key.class)
public class UserInterest {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Id
    @Column(name = "category_id")
    private Short categoryId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public UserInterest() {
    }

    public UserInterest(Long userId, Short categoryId) {
        this.userId = userId;
        this.categoryId = categoryId;
    }

    public Long getUserId() { return userId; }
    public Short getCategoryId() { return categoryId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    /** 복합 키 클래스 (JPA 규칙: public, 기본 생성자, equals/hashCode, Serializable) */
    public static class Key implements Serializable {
        private static final long serialVersionUID = 1L;
        private Long userId;
        private Short categoryId;

        public Key() {
        }

        public Key(Long userId, Short categoryId) {
            this.userId = userId;
            this.categoryId = categoryId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Key)) return false;
            Key k = (Key) o;
            return Objects.equals(userId, k.userId) && Objects.equals(categoryId, k.categoryId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(userId, categoryId);
        }
    }
}
