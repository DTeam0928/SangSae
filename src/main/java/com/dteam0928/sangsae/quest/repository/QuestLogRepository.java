package com.dteam0928.sangsae.quest.repository;

import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestLog;

import jakarta.persistence.EntityManager;

public class QuestLogRepository {

    // 완료한 퀘스트 QuestLog에 저장
    public QuestLog save(EntityManager em, QuestLog questLog) {
        em.persist(questLog);
        em.flush();
        return questLog;
    }

    // 전체 QuestLog 조회
    public List<QuestLog> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT ql FROM QuestLog ql ORDER BY ql.id",
                QuestLog.class
        ).getResultList();
    }

    // 특정 사용자의 QuestLog 조회
    public List<QuestLog> findByUserId(EntityManager em, Long userId) {
        return em.createQuery(
                "SELECT ql FROM QuestLog ql WHERE ql.userId = :userId ",
                QuestLog.class
        )
        .setParameter("userId", userId)
        .getResultList();
    }
}
