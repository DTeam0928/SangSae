package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.routine.entity.RoutineTemplate;
import com.dteam0928.sangsae.routine.entity.RoutineTemplateTask;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class RoutineTemplateRepository {

    public RoutineTemplate save(EntityManager em, RoutineTemplate template) {
        if (template.getId() == null) {
            em.persist(template);
        } else {
            template = em.merge(template);
        }
        em.flush();
        return template;
    }

    public RoutineTemplateTask saveTask(EntityManager em, RoutineTemplateTask task) {
        if (task.getId() == null) {
            em.persist(task);
        } else {
            task = em.merge(task);
        }
        em.flush();
        return task;
    }

    public RoutineTemplate findById(EntityManager em, Long id) {
        return em.find(RoutineTemplate.class, id);
    }

    public List<RoutineTemplate> findPublicTemplates(EntityManager em, String category) {
        if (category != null && !category.trim().isEmpty()) {
            return em.createQuery(
                "SELECT rt FROM RoutineTemplate rt "
                + "WHERE rt.visibility = 'PUBLIC' AND rt.category = :category "
                + "ORDER BY rt.adoptedCount DESC, rt.id DESC",
                RoutineTemplate.class
            )
            .setParameter("category", category.trim())
            .getResultList();
        }

        return em.createQuery(
            "SELECT rt FROM RoutineTemplate rt "
            + "WHERE rt.visibility = 'PUBLIC' "
            + "ORDER BY rt.adoptedCount DESC, rt.id DESC",
            RoutineTemplate.class
        ).getResultList();
    }

    public List<RoutineTemplateTask> findTasksByTemplateId(EntityManager em, Long routineTemplateId) {
        return em.createQuery(
            "SELECT rtt FROM RoutineTemplateTask rtt "
            + "WHERE rtt.routineTemplateId = :templateId "
            + "ORDER BY rtt.sortOrder ASC, rtt.defaultTime ASC",
            RoutineTemplateTask.class
        )
        .setParameter("templateId", routineTemplateId)
        .getResultList();
    }

    public void incrementAdoptedCount(EntityManager em, Long templateId) {
        em.createQuery(
            "UPDATE RoutineTemplate rt "
            + "SET rt.adoptedCount = rt.adoptedCount + 1 "
            + "WHERE rt.id = :id"
        )
        .setParameter("id", templateId)
        .executeUpdate();
    }
}
