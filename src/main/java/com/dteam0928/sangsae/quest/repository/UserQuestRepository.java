package com.dteam0928.sangsae.quest.repository;

import java.time.LocalDate;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.UserQuest;

import jakarta.persistence.EntityManager;

public class UserQuestRepository {

    public UserQuest save(EntityManager em, UserQuest userQuest) {
        em.persist(userQuest);
        em.flush();
        return userQuest;
    }

    public List<UserQuest> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT uq FROM UserQuest uq ORDER BY uq.id",
                UserQuest.class
        ).getResultList();
    }

    // 조건별 사용자 퀘스트 조회
    public List<UserQuest> findByUserIdAndTripIdAndScheduledDate(
            EntityManager em,
            Long userId,
            Long tripId,
            LocalDate scheduledDate) {

        if (tripId == null) {
            return em.createQuery(
                    "SELECT uq FROM UserQuest uq "
                    + "WHERE uq.userId = :userId "
                    + "AND uq.tripId IS NULL "
                    + "AND uq.scheduledDate = :scheduledDate "
                    + "ORDER BY uq.id",
                    UserQuest.class
            )
            .setParameter("userId", userId)
            .setParameter("scheduledDate", scheduledDate)
            .getResultList();
        }

        return em.createQuery(
                "SELECT uq FROM UserQuest uq "
                + "WHERE uq.userId = :userId "
                + "AND uq.tripId = :tripId "
                + "AND uq.scheduledDate = :scheduledDate "
                + "ORDER BY uq.id",
                UserQuest.class
        )
        .setParameter("userId", userId)
        .setParameter("tripId", tripId)
        .setParameter("scheduledDate", scheduledDate)
        .getResultList();
    }

    public UserQuest findById(EntityManager em, Long id) {
        return em.find(UserQuest.class, id);
    }
}
