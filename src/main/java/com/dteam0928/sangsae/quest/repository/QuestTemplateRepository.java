package com.dteam0928.sangsae.quest.repository;

import java.util.Collections;
import java.util.List;

import com.dteam0928.sangsae.quest.dto.QuestTemplate;

import jakarta.persistence.EntityManager;

public class QuestTemplateRepository {

    public List<QuestTemplate> findAll(EntityManager em) {
        return em.createQuery(
                "SELECT q FROM QuestTemplate q ORDER BY q.id",
                QuestTemplate.class
        ).getResultList();
    }

    public QuestTemplate findById(EntityManager em, Long id) {
        return em.find(QuestTemplate.class, id);
    }

    public List<QuestTemplate> findByIds(EntityManager em, List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }

        return em.createQuery(
                "SELECT q FROM QuestTemplate q WHERE q.id IN :ids ORDER BY q.id",
                QuestTemplate.class
        )
        .setParameter("ids", ids)
        .getResultList();
    }
}
