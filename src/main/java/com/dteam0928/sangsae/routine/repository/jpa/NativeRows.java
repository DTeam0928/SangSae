package com.dteam0928.sangsae.routine.repository.jpa;

import com.dteam0928.sangsae.routine.dto.PlaceSummary;
import jakarta.persistence.Query;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** native 쿼리 결과(Object[]) 변환과 파라미터 번호 관리 */
final class NativeRows {

    private NativeRows() {
    }

    static long l(Object o) {
        return ((Number) o).longValue();
    }

    static int i(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    static Integer roundedOrNull(Object o) {
        return o == null ? null : (int) Math.round(((Number) o).doubleValue());
    }

    static String s(Object o) {
        return o == null ? null : o.toString();
    }

    static boolean b(Object o) {
        if (o == null) return false;
        if (o instanceof Boolean bool) return bool;
        return ((Number) o).intValue() != 0;
    }

    /** DATE 컬럼 값. 드라이버·Hibernate 버전에 따라 java.sql.Date 또는 LocalDate 로 온다 */
    static java.time.LocalDate date(Object o) {
        if (o == null) return null;
        if (o instanceof java.time.LocalDate d) return d;
        if (o instanceof java.sql.Date d) return d.toLocalDate();
        if (o instanceof java.util.Date d) return new java.sql.Date(d.getTime()).toLocalDate();
        return java.time.LocalDate.parse(o.toString().substring(0, 10));
    }

    /** TIME 컬럼 값 → "HH:mm". (SQL 에서 TIME_FORMAT(..., '%H:%i') 을 쓰면 ':i' 가 Hibernate 이름 파라미터로 읽힐 수 있어 여기서 변환) */
    static String hhmm(Object o) {
        if (o == null) return null;
        java.time.LocalTime t;
        if (o instanceof java.time.LocalTime lt) t = lt;
        else if (o instanceof java.sql.Time st) t = st.toLocalTime();
        else if (o instanceof java.time.Duration d) t = java.time.LocalTime.MIDNIGHT.plus(d);
        else t = java.time.LocalTime.parse(o.toString());
        return String.format("%02d:%02d", t.getHour(), t.getMinute());
    }

    static List<String> csv(Object o) {
        return split(o, ",");
    }

    /** 구분자로 나눈 목록. 자유 태그는 쉼표가 들어갈 수 있어 "|" 로 묶는다 */
    static List<String> split(Object o, String separator) {
        if (o == null || o.toString().isBlank()) return List.of();
        return Arrays.stream(o.toString().split(java.util.regex.Pattern.quote(separator)))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    /** 장소 컬럼 묶음 (8칸). place_categories 를 LEFT JOIN pc 로 붙여야 한다 */
    static final String PLACE_COLUMNS =
            "p.id AS place_id, p.name AS place_name, p.category AS place_category, pc.name AS place_category_name,"
                    + " p.road_address, ST_Latitude(p.coordinates) AS place_lat, ST_Longitude(p.coordinates) AS place_lng";

    static final String JOIN_PLACE_CATEGORY = " LEFT JOIN place_categories pc ON pc.code = p.category";

    /** row[start..start+7] = id, name, category, category_name, road_address, lat, lng, dist (PLACE_COLUMNS + 거리) */
    static PlaceSummary place(Object[] row, int start) {
        if (row[start] == null) return null;
        return new PlaceSummary(l(row[start]), s(row[start + 1]), s(row[start + 2]), s(row[start + 3]),
                s(row[start + 4]), ((Number) row[start + 5]).doubleValue(), ((Number) row[start + 6]).doubleValue(),
                roundedOrNull(row[start + 7]));
    }

    @SuppressWarnings("unchecked")
    static List<Object[]> rows(Query q) {
        return (List<Object[]>) q.getResultList();
    }

    /** 위치 파라미터(?1, ?2 …)를 순서대로 쌓아 두고 마지막에 한 번에 바인딩 */
    static final class Params {
        private final List<Object> values = new ArrayList<>();

        /** 값을 추가하고 SQL 에 넣을 자리표시자("?n") 반환. 같은 값을 여러 곳에 쓰면 반환값을 재사용 */
        String add(Object value) {
            values.add(value);
            return "?" + values.size();
        }

        /** null 이면 SQL 리터럴 NULL (native 쿼리에 null 을 바인딩하면 타입 추론이 안 될 수 있어서) */
        String nullable(Object value) {
            return value == null ? "NULL" : add(value);
        }

        Query bind(Query q) {
            for (int idx = 0; idx < values.size(); idx++) q.setParameter(idx + 1, values.get(idx));
            return q;
        }
    }
}
