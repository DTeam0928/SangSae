package com.dteam0928.sangsae.routine.repository;

import com.dteam0928.sangsae.routine.entity.RoutineTaskSchedule;
import com.dteam0928.sangsae.routine.entity.UserRoutine;
import com.dteam0928.sangsae.routine.entity.UserRoutineTask;
import jakarta.persistence.EntityManager;
import java.util.Collections;
import java.util.List;

public class UserRoutineRepository {

    public UserRoutine saveRoutine(EntityManager em, UserRoutine userRoutine) {
        if (userRoutine.getId() == null) {
            em.persist(userRoutine);
        } else {
            userRoutine = em.merge(userRoutine);
        }
        em.flush();
        return userRoutine;
    }

    public UserRoutineTask saveTask(EntityManager em, UserRoutineTask task) {
        if (task.getId() == null) {
            em.persist(task);
        } else {
            task = em.merge(task);
        }
        em.flush();
        return task;
    }

    public void saveSchedules(EntityManager em, List<RoutineTaskSchedule> schedules) {
        if (schedules == null || schedules.isEmpty()) {
            return;
        }
        for (RoutineTaskSchedule schedule : schedules) {
            if (schedule.getId() == null) {
                em.persist(schedule);
            } else {
                em.merge(schedule);
            }
        }
        em.flush();
    }

    public UserRoutine findRoutineById(EntityManager em, Long id) {
        return em.find(UserRoutine.class, id);
    }

    public UserRoutineTask findTaskById(EntityManager em, Long id) {
        return em.find(UserRoutineTask.class, id);
    }

    public List<UserRoutine> findActiveRoutinesByUserId(EntityManager em, Long userId) {
        return em.createQuery(
            "SELECT ur FROM UserRoutine ur "
            + "WHERE ur.userId = :userId AND ur.isActive = true "
            + "ORDER BY ur.id DESC",
            UserRoutine.class
        )
        .setParameter("userId", userId)
        .getResultList();
    }

    public List<UserRoutineTask> findTasksByUserRoutineId(EntityManager em, Long userRoutineId) {
        return em.createQuery(
            "SELECT urt FROM UserRoutineTask urt "
            + "WHERE urt.userRoutineId = :routineId AND urt.isActive = true "
            + "ORDER BY urt.targetTime ASC, urt.id ASC",
            UserRoutineTask.class
        )
        .setParameter("routineId", userRoutineId)
        .getResultList();
    }

    public List<UserRoutineTask> findTasksByUserRoutineIds(EntityManager em, List<Long> userRoutineIds) {
        if (userRoutineIds == null || userRoutineIds.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery(
            "SELECT urt FROM UserRoutineTask urt "
            + "WHERE urt.userRoutineId IN :routineIds AND urt.isActive = true "
            + "ORDER BY urt.targetTime ASC, urt.id ASC",
            UserRoutineTask.class
        )
        .setParameter("routineIds", userRoutineIds)
        .getResultList();
    }

    public List<RoutineTaskSchedule> findSchedulesByTaskId(EntityManager em, Long taskId) {
        return em.createQuery(
            "SELECT rts FROM RoutineTaskSchedule rts "
            + "WHERE rts.userRoutineTaskId = :taskId",
            RoutineTaskSchedule.class
        )
        .setParameter("taskId", taskId)
        .getResultList();
    }

    public List<RoutineTaskSchedule> findSchedulesByTaskIds(EntityManager em, List<Long> taskIds) {
        if (taskIds == null || taskIds.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery(
            "SELECT rts FROM RoutineTaskSchedule rts "
            + "WHERE rts.userRoutineTaskId IN :taskIds",
            RoutineTaskSchedule.class
        )
        .setParameter("taskIds", taskIds)
        .getResultList();
    }
}
