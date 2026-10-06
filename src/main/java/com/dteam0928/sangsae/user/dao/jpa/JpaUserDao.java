package com.dteam0928.sangsae.user.dao.jpa;

import com.dteam0928.sangsae.user.dao.UserDao;
import com.dteam0928.sangsae.user.model.AuthProvider;
import com.dteam0928.sangsae.user.model.User;
import jakarta.persistence.EntityManager;

import java.util.Optional;

public class JpaUserDao implements UserDao {

    @Override
    public Optional<User> findById(EntityManager em, long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(EntityManager em, String email) {
        return em.createQuery("SELECT u FROM User u WHERE u.email = :email", User.class)
                .setParameter("email", email)
                .getResultList().stream().findFirst();
    }

    @Override
    public Optional<User> findByOAuth(EntityManager em, AuthProvider provider, String oauthId) {
        return em.createQuery(
                        "SELECT u FROM User u WHERE u.oauthProvider = :provider AND u.oauthId = :oauthId ORDER BY u.id",
                        User.class)
                .setParameter("provider", provider)
                .setParameter("oauthId", oauthId)
                .setMaxResults(1)
                .getResultList().stream().findFirst();
    }

    @Override
    public boolean existsByEmail(EntityManager em, String email) {
        return em.createQuery("SELECT COUNT(u) FROM User u WHERE u.email = :email", Long.class)
                .setParameter("email", email)
                .getSingleResult() > 0;
    }

    @Override
    public User insert(EntityManager em, User user) {
        em.persist(user);
        em.flush();   // IDENTITY id 확보 + UNIQUE 위반을 이 시점에 드러냄
        return user;
    }
}
