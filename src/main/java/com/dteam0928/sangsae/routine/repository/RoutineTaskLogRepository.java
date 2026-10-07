package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.routine.entity.RoutineTaskLog;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

public class RoutineTaskLogRepository {

    public RoutineTaskLog save(EntityManager em, RoutineTaskLog log) {
        if (log.getId() == null) {
            em.persist(log);
        } else {
            log = em.merge(log);
        }
        em.flush();
        return log;
    }

    public RoutineTaskLog findTodayLog(EntityManager em, Long userId, Long userRoutineTaskId, LocalDate date) {
        List<RoutineTaskLog> list = em.createQuery(
            "SELECT rtl FROM RoutineTaskLog rtl "
            + "WHERE rtl.userId = :userId "
            + "AND rtl.userRoutineTaskId = :taskId "
            + "AND rtl.logDate = :logDate "
            + "AND rtl.isSuccess = true",
            RoutineTaskLog.class
        )
        .setParameter("userId", userId)
        .setParameter("taskId", userRoutineTaskId)
        .setParameter("logDate", date)
        .setMaxResults(1)
        .getResultList();

        return list.isEmpty() ? null : list.get(0);
    }

    public List<RoutineTaskLog> findTodayLogsByUser(EntityManager em, Long userId, LocalDate date) {
        return em.createQuery(
            "SELECT rtl FROM RoutineTaskLog rtl "
            + "WHERE rtl.userId = :userId "
            + "AND rtl.logDate = :logDate "
            + "AND rtl.isSuccess = true",
            RoutineTaskLog.class
        )
        .setParameter("userId", userId)
        .setParameter("logDate", date)
        .getResultList();
    }
}
