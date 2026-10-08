package com.dteam0928.sangsae.routine.repository.jpa;

import com.dteam0928.sangsae.routine.repository.LogRow;
import com.dteam0928.sangsae.routine.repository.MyRoutineRepository;
import com.dteam0928.sangsae.routine.repository.RoutineRow;
import com.dteam0928.sangsae.routine.repository.TaskRow;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static com.dteam0928.sangsae.routine.repository.jpa.NativeRows.*;

public class JpaMyRoutineRepository implements MyRoutineRepository {

    // ------------------------------------------------------------------ 루틴

    /** 0~10 루틴·템플릿, 11~18 대표 장소(PLACE_COLUMNS + 거리 자리) */
    private static final String ROUTINE_SELECT =
            "SELECT ur.id, ur.title, ur.start_date, ur.end_date, ur.origin_template_id, rt.creator_id, rt.visibility,"
                    + " rt.description,"
                    + " (SELECT GROUP_CONCAT(c.category_code ORDER BY rc.sort_order) FROM routine_template_categories c"
                    + "   JOIN routine_categories rc ON rc.code = c.category_code WHERE c.routine_template_id = rt.id),"
                    + " (SELECT GROUP_CONCAT(g.tag ORDER BY g.tag SEPARATOR '|') FROM routine_template_tags g"
                    + "   WHERE g.routine_template_id = rt.id),"
                    + " (SELECT COUNT(*) FROM user_routine_tasks t WHERE t.user_routine_id = ur.id AND t.is_active = 1),"
                    + " " + PLACE_COLUMNS + ", NULL"
                    + " FROM user_routines ur"
                    + " LEFT JOIN routine_templates rt ON rt.id = ur.origin_template_id"
                    + " LEFT JOIN places p ON p.id = rt.representative_place_id" + JOIN_PLACE_CATEGORY
                    + " WHERE ur.user_id = ?1 AND ur.is_active = 1";

    @Override
    public List<RoutineRow> findRoutines(EntityManager em, long userId) {
        return rows(em.createNativeQuery(ROUTINE_SELECT + " ORDER BY ur.created_at, ur.id").setParameter(1, userId))
                .stream().map(JpaMyRoutineRepository::routine).toList();
    }

    @Override
    public RoutineRow findRoutine(EntityManager em, long userId, long routineId) {
        List<Object[]> rows = rows(em.createNativeQuery(ROUTINE_SELECT + " AND ur.id = ?2")
                .setParameter(1, userId).setParameter(2, routineId));
        return rows.isEmpty() ? null : routine(rows.get(0));
    }

    private static RoutineRow routine(Object[] r) {
        return new RoutineRow(l(r[0]), s(r[1]), date(r[2]), date(r[3]),
                r[4] == null ? null : l(r[4]), r[5] == null ? null : l(r[5]), s(r[6]), s(r[7]),
                csv(r[8]), split(r[9], "|"), place(r, 11), i(r[10]));
    }

    @Override
    public long insertRoutine(EntityManager em, long userId, Long templateId, String title, LocalDate start, LocalDate end) {
        Params p = new Params();
        String sql = "INSERT INTO user_routines (user_id, origin_template_id, title, start_date, end_date) VALUES ("
                + p.add(userId) + ", " + p.nullable(templateId) + ", " + p.add(title) + ", " + p.add(start) + ", "
                + p.nullable(end) + ")";
        p.bind(em.createNativeQuery(sql)).executeUpdate();
        return lastId(em);
    }

    @Override
    public void updateRoutine(EntityManager em, long routineId, String title, LocalDate start, LocalDate end) {
        Params p = new Params();
        String sql = "UPDATE user_routines SET title = " + p.add(title) + ", start_date = " + p.add(start)
                + ", end_date = " + p.nullable(end) + " WHERE id = " + p.add(routineId);
        p.bind(em.createNativeQuery(sql)).executeUpdate();
    }

    @Override
    public void linkTemplate(EntityManager em, long routineId, long templateId) {
        em.createNativeQuery("UPDATE user_routines SET origin_template_id = ?1 WHERE id = ?2")
                .setParameter(1, templateId).setParameter(2, routineId).executeUpdate();
    }

    @Override
    public void deactivateRoutine(EntityManager em, long routineId) {
        em.createNativeQuery("UPDATE user_routines SET is_active = 0 WHERE id = ?1")
                .setParameter(1, routineId).executeUpdate();
    }

    // ------------------------------------------------------------------ 탐색용 사본

    @Override
    public long insertTemplate(EntityManager em, long creatorId, PublicInfo info) {
        Params p = new Params();
        String sql = "INSERT INTO routine_templates (creator_id, representative_place_id, title, description, category,"
                + " visibility) VALUES (" + p.add(creatorId) + ", " + p.nullable(info.representativePlaceId()) + ", "
                + p.add(info.title()) + ", " + p.nullable(info.description()) + ", " + p.add(info.mainCategory()) + ", "
                + p.add(info.visibility()) + ")";
        p.bind(em.createNativeQuery(sql)).executeUpdate();
        long id = lastId(em);
        replaceTemplateLabels(em, id, info);
        return id;
    }

    @Override
    public void updateTemplate(EntityManager em, long templateId, PublicInfo info) {
        Params p = new Params();
        String sql = "UPDATE routine_templates SET representative_place_id = " + p.nullable(info.representativePlaceId())
                + ", title = " + p.add(info.title()) + ", description = " + p.nullable(info.description())
                + ", category = " + p.add(info.mainCategory()) + ", visibility = " + p.add(info.visibility())
                + " WHERE id = " + p.add(templateId);
        p.bind(em.createNativeQuery(sql)).executeUpdate();
        replaceTemplateLabels(em, templateId, info);
    }

    /** 카테고리·태그는 지우고 다시 넣는다 */
    private static void replaceTemplateLabels(EntityManager em, long templateId, PublicInfo info) {
        em.createNativeQuery("DELETE FROM routine_template_categories WHERE routine_template_id = ?1")
                .setParameter(1, templateId).executeUpdate();
        for (String code : info.categoryCodes()) {
            em.createNativeQuery("INSERT INTO routine_template_categories (routine_template_id, category_code) VALUES (?1, ?2)")
                    .setParameter(1, templateId).setParameter(2, code).executeUpdate();
        }
        em.createNativeQuery("DELETE FROM routine_template_tags WHERE routine_template_id = ?1")
                .setParameter(1, templateId).executeUpdate();
        for (String tag : info.tags()) {
            em.createNativeQuery("INSERT INTO routine_template_tags (routine_template_id, tag) VALUES (?1, ?2)")
                    .setParameter(1, templateId).setParameter(2, tag).executeUpdate();
        }
    }

    @Override
    public void setTemplateVisibility(EntityManager em, long templateId, String visibility) {
        em.createNativeQuery("UPDATE routine_templates SET visibility = ?1 WHERE id = ?2")
                .setParameter(1, visibility).setParameter(2, templateId).executeUpdate();
    }

    @Override
    public long insertTemplateTask(EntityManager em, long templateId, Long placeId, String content, LocalTime time,
                                   String category) {
        Params p = new Params();
        String sql = "INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category)"
                + " VALUES (" + p.add(templateId) + ", " + p.nullable(placeId) + ", " + p.add(content) + ", "
                + p.add(time) + ", " + p.add(category) + ")";
        p.bind(em.createNativeQuery(sql)).executeUpdate();
        return lastId(em);
    }

    @Override
    public void updateTemplateTask(EntityManager em, long templateId, long templateTaskId, Long placeId, String content,
                                   LocalTime time, String category) {
        Params p = new Params();
        String sql = "UPDATE routine_template_tasks SET place_id = " + p.nullable(placeId) + ", content = " + p.add(content)
                + ", default_time = " + p.add(time) + ", category = " + p.add(category)
                + " WHERE id = " + p.add(templateTaskId) + " AND routine_template_id = " + p.add(templateId);
        p.bind(em.createNativeQuery(sql)).executeUpdate();
    }

    @Override
    public void deleteTemplateTask(EntityManager em, long templateId, long templateTaskId) {
        em.createNativeQuery("DELETE FROM routine_template_tasks WHERE id = ?1 AND routine_template_id = ?2")
                .setParameter(1, templateTaskId).setParameter(2, templateId).executeUpdate();
    }

    @Override
    public void resequenceTemplateTasks(EntityManager em, long templateId) {
        em.createNativeQuery("UPDATE routine_template_tasks k JOIN ("
                        + " SELECT id, ROW_NUMBER() OVER (ORDER BY default_time, id) AS rn"
                        + " FROM routine_template_tasks WHERE routine_template_id = ?1) x ON x.id = k.id"
                        + " SET k.sort_order = x.rn")
                .setParameter(1, templateId).executeUpdate();
    }

    // ------------------------------------------------------------------ Task

    /** 0~13 Task(13 = 인증 반경), 14~21 장소(PLACE_COLUMNS + 거리 자리) */
    private static final String TASK_SELECT =
            "SELECT t.id, t.content, t.target_time, t.category, rc.code, rc.emoji, t.repeat_type,"
                    + " t.once_date, t.alert_time, t.memo, DATE(t.created_at),"
                    + " t.origin_template_task_id,"
                    + " (SELECT GROUP_CONCAT(s.day_of_week) FROM routine_task_schedules s"
                    + "   WHERE s.user_routine_task_id = t.id), t.allowed_radius_meters,"
                    + " " + PLACE_COLUMNS + ", NULL"
                    + " FROM user_routine_tasks t"
                    + " LEFT JOIN routine_categories rc ON rc.name = t.category"
                    + " LEFT JOIN places p ON p.id = t.place_id" + JOIN_PLACE_CATEGORY
                    + " WHERE t.user_routine_id = ?1 AND t.is_active = 1";

    @Override
    public List<TaskRow> findTasks(EntityManager em, long routineId) {
        return rows(em.createNativeQuery(TASK_SELECT + " ORDER BY t.target_time, t.id").setParameter(1, routineId))
                .stream().map(JpaMyRoutineRepository::task).toList();
    }

    @Override
    public TaskRow findTask(EntityManager em, long routineId, long taskId) {
        List<Object[]> rows = rows(em.createNativeQuery(TASK_SELECT + " AND t.id = ?2")
                .setParameter(1, routineId).setParameter(2, taskId));
        return rows.isEmpty() ? null : task(rows.get(0));
    }

    private static TaskRow task(Object[] r) {
        return new TaskRow(l(r[0]), s(r[1]), hhmm(r[2]), s(r[3]), s(r[4]), s(r[5]), s(r[6]), date(r[7]), hhmm(r[8]), s(r[9]),
                date(r[10]), r[11] == null ? null : l(r[11]), csv(r[12]), i(r[13]), place(r, 14));
    }

    @Override
    public long insertTask(EntityManager em, long routineId, TaskValues v) {
        Params p = new Params();
        String sql = "INSERT INTO user_routine_tasks (user_routine_id, place_id, content, target_time, category,"
                + " repeat_type, once_date, alert_time, memo, reward_xp, allowed_radius_meters) VALUES (" + p.add(routineId) + ", "
                + p.nullable(v.placeId()) + ", " + p.add(v.content()) + ", " + p.add(v.time()) + ", "
                + p.add(v.category()) + ", " + p.add(v.repeatType()) + ", " + p.nullable(v.onceDate()) + ", "
                + p.nullable(v.alertTime()) + ", " + p.nullable(v.memo()) + ", " + p.add(v.rewardXp()) + ", "
                + p.add(v.allowedRadiusMeters()) + ")";
        p.bind(em.createNativeQuery(sql)).executeUpdate();
        return lastId(em);
    }

    @Override
    public void updateTask(EntityManager em, long taskId, TaskValues v) {
        Params p = new Params();
        String sql = "UPDATE user_routine_tasks SET place_id = " + p.nullable(v.placeId()) + ", content = "
                + p.add(v.content()) + ", target_time = " + p.add(v.time()) + ", category = " + p.add(v.category())
                + ", repeat_type = " + p.add(v.repeatType()) + ", once_date = " + p.nullable(v.onceDate())
                + ", alert_time = " + p.nullable(v.alertTime()) + ", memo = " + p.nullable(v.memo())
                + ", allowed_radius_meters = " + p.add(v.allowedRadiusMeters())
                + " WHERE id = " + p.add(taskId);
        p.bind(em.createNativeQuery(sql)).executeUpdate();
    }

    @Override
    public void linkTemplateTask(EntityManager em, long taskId, long templateTaskId) {
        em.createNativeQuery("UPDATE user_routine_tasks SET origin_template_task_id = ?1 WHERE id = ?2")
                .setParameter(1, templateTaskId).setParameter(2, taskId).executeUpdate();
    }

    @Override
    public void deactivateTask(EntityManager em, long taskId) {
        em.createNativeQuery("UPDATE user_routine_tasks SET is_active = 0 WHERE id = ?1")
                .setParameter(1, taskId).executeUpdate();
    }

    @Override
    public void replaceSchedules(EntityManager em, long taskId, List<String> daysOfWeek) {
        em.createNativeQuery("DELETE FROM routine_task_schedules WHERE user_routine_task_id = ?1")
                .setParameter(1, taskId).executeUpdate();
        for (String day : daysOfWeek) {
            em.createNativeQuery("INSERT INTO routine_task_schedules (user_routine_task_id, day_of_week) VALUES (?1, ?2)")
                    .setParameter(1, taskId).setParameter(2, day).executeUpdate();
        }
    }

    // ------------------------------------------------------------------ 기록·통계

    @Override
    public List<LogRow> findLogs(EntityManager em, long userId, long routineId, LocalDate from, LocalDate to) {
        return rows(em.createNativeQuery("SELECT l.user_routine_task_id, l.log_date, l.completed_at"
                        + " FROM routine_task_logs l JOIN user_routine_tasks t ON t.id = l.user_routine_task_id"
                        + " WHERE t.user_routine_id = ?1 AND l.user_id = ?2 AND l.is_success = 1"
                        + " AND l.log_date BETWEEN ?3 AND ?4")
                .setParameter(1, routineId).setParameter(2, userId).setParameter(3, from).setParameter(4, to))
                .stream().map(r -> new LogRow(l(r[0]), date(r[1]), hhmm(r[2]))).toList();
    }

    @Override
    public List<LocalDate> findLogDates(EntityManager em, long userId, long routineId, LocalDate to, int limit) {
        List<?> list = em.createNativeQuery("SELECT DISTINCT l.log_date"
                        + " FROM routine_task_logs l JOIN user_routine_tasks t ON t.id = l.user_routine_task_id"
                        + " WHERE t.user_routine_id = ?1 AND l.user_id = ?2 AND l.is_success = 1 AND l.log_date <= ?3"
                        + " ORDER BY l.log_date DESC")
                .setParameter(1, routineId).setParameter(2, userId).setParameter(3, to)
                .setMaxResults(limit).getResultList();
        return list.stream().map(NativeRows::date).toList();
    }

    @Override
    public int findTotalXp(EntityManager em, long userId) {
        List<?> list = em.createNativeQuery("SELECT total_xp FROM user_profiles WHERE user_id = ?1")
                .setParameter(1, userId).getResultList();
        return list.isEmpty() ? 0 : i(list.get(0));
    }

    @Override
    public boolean existsPlace(EntityManager em, long placeId) {
        return i(em.createNativeQuery("SELECT COUNT(*) FROM places WHERE id = ?1")
                .setParameter(1, placeId).getSingleResult()) > 0;
    }

    private static long lastId(EntityManager em) {
        return l(em.createNativeQuery("SELECT LAST_INSERT_ID()").getSingleResult());
    }
}
