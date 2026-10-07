package com.dteam0928.sangsae.routine.repository.jpa;

import com.dteam0928.sangsae.common.geo.GeoPoint;
import com.dteam0928.sangsae.routine.dto.PlaceCategoryDto;
import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import com.dteam0928.sangsae.routine.dto.RoutineCard;
import com.dteam0928.sangsae.routine.dto.RoutineCategoryDto;
import com.dteam0928.sangsae.routine.dto.RoutineTaskDto;
import com.dteam0928.sangsae.routine.repository.CardQuery;
import com.dteam0928.sangsae.routine.repository.PlaceRoutineRow;
import com.dteam0928.sangsae.routine.repository.PlaceUsage;
import com.dteam0928.sangsae.routine.repository.RoutineExploreRepository;
import com.dteam0928.sangsae.routine.repository.jpa.NativeRows.Params;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.dteam0928.sangsae.routine.repository.jpa.NativeRows.*;

/**
 * 루틴 탐색 repository. 거리·집계·적합도 계산이 많아 전부 native SQL 로 처리한다.
 * 좌표 규칙: places.coordinates = POINT(위도 경도), SRID 4326. 거리는 ST_Distance(미터).
 */
public class JpaRoutineExploreRepository implements RoutineExploreRepository {

    /** 루틴-장소 연결: 대표 장소 + Task 별 장소 */
    private static final String TEMPLATE_PLACES =
            "(SELECT id AS template_id, representative_place_id AS place_id FROM routine_templates"
                    + " WHERE representative_place_id IS NOT NULL"
                    + " UNION SELECT routine_template_id, place_id FROM routine_template_tasks WHERE place_id IS NOT NULL)";

    /** 내 관심사(O-02)와 연결된 루틴 카테고리 코드 */
    private static String myCategories(String userParam) {
        return "SELECT m.routine_category_code FROM interest_routine_category_map m"
                + " JOIN user_interests ui ON ui.category_id = m.interest_category_id WHERE ui.user_id = " + userParam;
    }

    /** 카테고리 수 / 내 관심사와 맞는 카테고리 수 (적합도 재료) */
    private static String fitColumns(String userParam) {
        return "(SELECT COUNT(*) FROM routine_template_categories rc WHERE rc.routine_template_id = t.id) AS tag_total, "
                + "(SELECT COUNT(*) FROM routine_template_categories rc WHERE rc.routine_template_id = t.id"
                + " AND rc.category_code IN (" + myCategories(userParam) + ")) AS tag_match";
    }

    private static final String CATEGORY_CSV =
            "(SELECT GROUP_CONCAT(rc.category_code) FROM routine_template_categories rc WHERE rc.routine_template_id = t.id)";

    /** 자유 태그. 태그에 쉼표가 들어갈 수 있어 "|" 로 묶는다 */
    private static final String TAG_LIST =
            "(SELECT GROUP_CONCAT(tg.tag ORDER BY tg.tag SEPARATOR '|') FROM routine_template_tags tg"
                    + " WHERE tg.routine_template_id = t.id)";

    // ------------------------------------------------------------------ 이름표

    @Override
    public List<RoutineCategoryDto> findCategories(EntityManager em) {
        List<RoutineCategoryDto> list = new ArrayList<>();
        for (Object[] r : rows(em.createNativeQuery(
                "SELECT code, name, emoji FROM routine_categories ORDER BY sort_order, code"))) {
            list.add(new RoutineCategoryDto(s(r[0]), s(r[1]), s(r[2])));
        }
        return list;
    }

    @Override
    public List<PlaceCategoryDto> findPlaceCategories(EntityManager em) {
        List<PlaceCategoryDto> list = new ArrayList<>();
        for (Object[] r : rows(em.createNativeQuery(
                "SELECT code, name, emoji FROM place_categories ORDER BY sort_order, code"))) {
            list.add(new PlaceCategoryDto(s(r[0]), s(r[1]), s(r[2])));
        }
        return list;
    }

    private Map<String, RoutineCategoryDto> categoryMap(EntityManager em) {
        Map<String, RoutineCategoryDto> map = new LinkedHashMap<>();
        for (RoutineCategoryDto c : findCategories(em)) map.put(c.code(), c);
        return map;
    }

    // ------------------------------------------------------------------ 카드

    /**
     * 카드 SELECT. 컬럼 순서:
     * 0 id, 1 title, 2 description, 3 adopted_count, 4 created_at,
     * 5~12 장소(id, name, category, category_name, road_address, lat, lng, dist),
     * 13 task_count, 14 cats, 15 tags, 16 tag_total, 17 tag_match, 18 bookmarked, 19 recent_adopted
     */
    private static String cardSelect(Params p, long userId, GeoPoint origin, int trendingDays) {
        String u = p.add(userId);
        String o = p.add(origin.toWkt());
        String d = p.add(trendingDays);
        return "SELECT t.id, t.title, t.description, t.adopted_count,"
                + " DATE_FORMAT(t.created_at, '%Y-%m-%dT%H:%i:%s') AS created_at, "
                + PLACE_COLUMNS + ","
                + " CASE WHEN p.id IS NULL THEN NULL ELSE ST_Distance(p.coordinates, ST_GeomFromText(" + o + ", 4326)) END AS dist,"
                + " (SELECT COUNT(*) FROM routine_template_tasks k WHERE k.routine_template_id = t.id) AS task_count, "
                + CATEGORY_CSV + " AS cats, "
                + TAG_LIST + " AS tags, "
                + fitColumns(u) + ","
                + " EXISTS (SELECT 1 FROM routine_template_bookmarks b WHERE b.user_id = " + u
                + " AND b.routine_template_id = t.id) AS bookmarked,"
                + " (SELECT COUNT(*) FROM user_routines ur WHERE ur.origin_template_id = t.id"
                + " AND ur.created_at >= NOW() - INTERVAL " + d + " DAY) AS recent_adopted"
                + " FROM routine_templates t LEFT JOIN places p ON p.id = t.representative_place_id"
                + JOIN_PLACE_CATEGORY
                + " WHERE t.visibility = 'PUBLIC'";
    }

    private static RoutineCard toCard(Object[] r, Map<String, RoutineCategoryDto> categories) {
        int tagTotal = i(r[16]);
        int fit = tagTotal == 0 ? 0 : (int) Math.round(100.0 * i(r[17]) / tagTotal);
        List<RoutineCategoryDto> cats = csv(r[14]).stream()
                .map(categories::get).filter(c -> c != null).toList();
        return new RoutineCard(l(r[0]), s(r[1]), s(r[2]), cats, split(r[15], "|"), place(r, 5), i(r[13]),
                i(r[3]), i(r[19]), fit, b(r[18]), s(r[4]));
    }

    @Override
    public List<RoutineCard> findCards(EntityManager em, CardQuery q) {
        Params p = new Params();
        StringBuilder inner = new StringBuilder(cardSelect(p, q.userId(), q.origin(), q.trendingDays()));

        if (q.keyword() != null) {
            String kw = p.add("%" + escapeLike(q.keyword()) + "%");
            inner.append(" AND (t.title LIKE ").append(kw)
                    .append(" OR t.description LIKE ").append(kw)
                    .append(" OR p.name LIKE ").append(kw)
                    .append(" OR EXISTS (SELECT 1 FROM routine_template_categories rc JOIN routine_categories c")
                    .append(" ON c.code = rc.category_code WHERE rc.routine_template_id = t.id AND c.name LIKE ")
                    .append(kw).append(")")
                    .append(" OR EXISTS (SELECT 1 FROM routine_template_tags tg")
                    .append(" WHERE tg.routine_template_id = t.id AND tg.tag LIKE ").append(kw).append("))");
        }
        if (q.categoryCode() != null) {
            inner.append(" AND EXISTS (SELECT 1 FROM routine_template_categories rc")
                    .append(" WHERE rc.routine_template_id = t.id AND rc.category_code = ")
                    .append(p.add(q.categoryCode())).append(")");
        }
        if (q.placeCategory() != null) {
            inner.append(" AND p.category = ").append(p.add(q.placeCategory()));
        }
        if (q.excludeAdopted()) {
            inner.append(" AND NOT EXISTS (SELECT 1 FROM user_routines ur WHERE ur.user_id = ")
                    .append(p.add(q.userId())).append(" AND ur.origin_template_id = t.id)");
        }

        StringBuilder sql = new StringBuilder("SELECT * FROM (").append(inner).append(") x");
        if (q.radiusMeters() != null) {
            sql.append(" WHERE x.dist IS NOT NULL AND x.dist <= ").append(p.add(q.radiusMeters()));
        }
        sql.append(" ORDER BY ").append(switch (q.sort()) {
            case DISTANCE -> "x.dist IS NULL, x.dist, x.adopted_count DESC, x.id DESC";
            case POPULAR -> "x.adopted_count DESC, x.created_at DESC, x.id DESC";
            case LATEST -> "x.created_at DESC, x.id DESC";
            case TRENDING -> "x.recent_adopted DESC, x.adopted_count DESC, x.dist IS NULL, x.dist, x.id DESC";
            case RECOMMEND -> "(x.tag_match / NULLIF(x.tag_total, 0)) DESC, x.adopted_count DESC, x.id DESC";
        });

        Query query = p.bind(em.createNativeQuery(sql.toString()))
                .setFirstResult(q.offset())
                .setMaxResults(q.limit());
        Map<String, RoutineCategoryDto> categories = categoryMap(em);
        return rows(query).stream().map(r -> toCard(r, categories)).toList();
    }

    @Override
    public Optional<RoutineCard> findCard(EntityManager em, long userId, long templateId, GeoPoint origin,
                                          int trendingDays) {
        Params p = new Params();
        String sql = cardSelect(p, userId, origin, trendingDays) + " AND t.id = " + p.add(templateId);
        List<Object[]> result = rows(p.bind(em.createNativeQuery(sql)));
        if (result.isEmpty()) return Optional.empty();
        return Optional.of(toCard(result.get(0), categoryMap(em)));
    }

    /** LIKE 특수문자(%, _, \) 를 글자 그대로 검색되게 */
    private static String escapeLike(String s) {
        return s.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    // ------------------------------------------------------------------ 상세

    @Override
    public List<RoutineTaskDto> findTasks(EntityManager em, long templateId) {
        List<RoutineTaskDto> list = new ArrayList<>();
        for (Object[] r : rows(em.createNativeQuery(
                        "SELECT k.id, k.content, TIME_FORMAT(k.default_time, '%H:%i'), k.category, k.reward_xp, p.name"
                                + " FROM routine_template_tasks k LEFT JOIN places p ON p.id = k.place_id"
                                + " WHERE k.routine_template_id = ?1 ORDER BY k.sort_order, k.default_time, k.id")
                .setParameter(1, templateId))) {
            list.add(new RoutineTaskDto(l(r[0]), s(r[1]), s(r[2]), s(r[3]), i(r[4]), s(r[5])));
        }
        return list;
    }

    @Override
    public int countPerformers(EntityManager em, long templateId) {
        return i(em.createNativeQuery("SELECT COUNT(DISTINCT user_id) FROM user_routines"
                        + " WHERE origin_template_id = ?1 AND is_active = 1")
                .setParameter(1, templateId).getSingleResult());
    }

    @Override
    public boolean hasActiveFork(EntityManager em, long userId, long templateId) {
        return i(em.createNativeQuery("SELECT COUNT(*) FROM user_routines"
                        + " WHERE user_id = ?1 AND origin_template_id = ?2 AND is_active = 1")
                .setParameter(1, userId).setParameter(2, templateId).getSingleResult()) > 0;
    }

    // ------------------------------------------------------------------ 장소

    @Override
    public List<PlaceSummary> findPlacesWithRoutines(EntityManager em, GeoPoint origin, int radiusMeters,
                                                     String placeCategory, int limit) {
        Params p = new Params();
        String o = p.add(origin.toWkt());
        StringBuilder sql = new StringBuilder("SELECT ").append(PLACE_COLUMNS)
                .append(", ST_Distance(p.coordinates, ST_GeomFromText(").append(o).append(", 4326)) AS dist")
                .append(" FROM places p").append(JOIN_PLACE_CATEGORY)
                .append(" WHERE p.id IN (SELECT tp.place_id FROM ").append(TEMPLATE_PLACES).append(" tp")
                .append(" JOIN routine_templates t ON t.id = tp.template_id WHERE t.visibility = 'PUBLIC')");
        if (placeCategory != null) sql.append(" AND p.category = ").append(p.add(placeCategory));
        sql.append(" HAVING dist <= ").append(p.add(radiusMeters)).append(" ORDER BY dist");

        Query q = p.bind(em.createNativeQuery(sql.toString())).setMaxResults(limit);
        return rows(q).stream().map(r -> place(r, 0)).toList();
    }

    @Override
    public Optional<PlaceSummary> findPlace(EntityManager em, long placeId, GeoPoint origin) {
        List<Object[]> result = rows(em.createNativeQuery("SELECT " + PLACE_COLUMNS + ","
                        + " ST_Distance(p.coordinates, ST_GeomFromText(?1, 4326)) FROM places p" + JOIN_PLACE_CATEGORY
                        + " WHERE p.id = ?2")
                .setParameter(1, origin.toWkt())
                .setParameter(2, placeId));
        return result.isEmpty() ? Optional.empty() : Optional.of(place(result.get(0), 0));
    }

    @Override
    public List<PlaceRoutineRow> findRoutinesByPlaces(EntityManager em, long userId, Collection<Long> placeIds) {
        if (placeIds.isEmpty()) return List.of();
        Params p = new Params();
        String u = p.add(userId);
        String sql = "SELECT tp.place_id, t.id, t.title, t.adopted_count,"
                + " (SELECT COUNT(DISTINCT ur.user_id) FROM user_routines ur"
                + " WHERE ur.origin_template_id = t.id AND ur.is_active = 1) AS performers, "
                + CATEGORY_CSV + " AS cats, "
                + TAG_LIST + " AS tags, "
                + fitColumns(u)
                + " FROM " + TEMPLATE_PLACES + " tp JOIN routine_templates t ON t.id = tp.template_id"
                + " WHERE t.visibility = 'PUBLIC' AND tp.place_id IN (" + p.add(List.copyOf(placeIds)) + ")";
        List<PlaceRoutineRow> list = new ArrayList<>();
        for (Object[] r : rows(p.bind(em.createNativeQuery(sql)))) {
            list.add(new PlaceRoutineRow(l(r[0]), l(r[1]), s(r[2]), i(r[3]), i(r[4]), csv(r[5]), split(r[6], "|"),
                    i(r[7]), i(r[8])));
        }
        return list;
    }

    @Override
    public Map<Long, Map<Integer, Integer>> findTaskHoursByPlaces(EntityManager em, Collection<Long> placeIds) {
        Map<Long, Map<Integer, Integer>> result = new HashMap<>();
        if (placeIds.isEmpty()) return result;
        for (Object[] r : rows(em.createNativeQuery(
                        "SELECT tp.place_id, HOUR(k.default_time) AS h, COUNT(*) FROM " + TEMPLATE_PLACES + " tp"
                                + " JOIN routine_templates t ON t.id = tp.template_id AND t.visibility = 'PUBLIC'"
                                + " JOIN routine_template_tasks k ON k.routine_template_id = t.id"
                                + " WHERE tp.place_id IN (?1) GROUP BY tp.place_id, h")
                .setParameter(1, List.copyOf(placeIds)))) {
            result.computeIfAbsent(l(r[0]), k -> new HashMap<>()).put(i(r[1]), i(r[2]));
        }
        return result;
    }

    @Override
    public Map<Long, PlaceUsage> findUsageByPlaces(EntityManager em, Collection<Long> placeIds, int days) {
        Map<Long, PlaceUsage> result = new HashMap<>();
        if (placeIds.isEmpty()) return result;
        // 오늘 포함 최근 days 일 = recent, 그 전 days 일 = previous
        for (Object[] r : rows(em.createNativeQuery(
                        "SELECT urt.place_id, HOUR(l.completed_at) AS h,"
                                + " SUM(CASE WHEN l.log_date > CURDATE() - INTERVAL ?1 DAY THEN 1 ELSE 0 END) AS recent,"
                                + " SUM(CASE WHEN l.log_date <= CURDATE() - INTERVAL ?1 DAY THEN 1 ELSE 0 END) AS previous"
                                + " FROM routine_task_logs l JOIN user_routine_tasks urt ON urt.id = l.user_routine_task_id"
                                + " WHERE l.is_success = 1 AND l.log_date > CURDATE() - INTERVAL ?2 DAY"
                                + " AND urt.place_id IN (?3)"
                                + " GROUP BY urt.place_id, h")
                .setParameter(1, days)
                .setParameter(2, days * 2)
                .setParameter(3, List.copyOf(placeIds)))) {
            PlaceUsage usage = result.computeIfAbsent(l(r[0]), k -> PlaceUsage.empty());
            int hour = i(r[1]);
            usage.recent().merge(hour, i(r[2]), Integer::sum);
            usage.previous().merge(hour, i(r[3]), Integer::sum);
        }
        return result;
    }

    // ------------------------------------------------------------------ 찜

    @Override
    public boolean existsPublicTemplate(EntityManager em, long templateId) {
        return i(em.createNativeQuery(
                        "SELECT COUNT(*) FROM routine_templates WHERE id = ?1 AND visibility = 'PUBLIC'")
                .setParameter(1, templateId).getSingleResult()) > 0;
    }

    @Override
    public void addBookmark(EntityManager em, long userId, long templateId) {
        em.createNativeQuery("INSERT IGNORE INTO routine_template_bookmarks (user_id, routine_template_id)"
                        + " VALUES (?1, ?2)")
                .setParameter(1, userId).setParameter(2, templateId).executeUpdate();
    }

    @Override
    public void removeBookmark(EntityManager em, long userId, long templateId) {
        em.createNativeQuery("DELETE FROM routine_template_bookmarks WHERE user_id = ?1 AND routine_template_id = ?2")
                .setParameter(1, userId).setParameter(2, templateId).executeUpdate();
    }
}
