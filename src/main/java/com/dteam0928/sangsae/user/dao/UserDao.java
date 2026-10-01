package com.dteam0928.sangsae.user.dao;

import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.user.model.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;

/**
 * users 접근.
 * 모든 메서드는 Service 가 넘겨준 EntityManager 를 사용한다(트랜잭션은 Service 가 관리).
 */
public interface UserDao {

    /** 제약 이름 (DB 설계서 4. 인덱스 명세) */
    String UK_EMAIL = "uk_users_email";
    String UK_OAUTH = "uk_users_oauth";

    Optional<User> findById(EntityManager em, long id);

    Optional<User> findByEmail(EntityManager em, String email);

    Optional<User> findByOAuth(EntityManager em, AuthProvider provider, String oauthId);

    boolean existsByEmail(EntityManager em, String email);

    /** INSERT 후 즉시 flush 해서 id 를 채운다 */
    User insert(EntityManager em, User user);
}
