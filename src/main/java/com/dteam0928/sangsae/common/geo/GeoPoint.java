package com.dteam0928.sangsae.common.geo;

import java.io.Serializable;
import java.util.Locale;

/**
 * 위경도 좌표 (WGS84, SRID 4326). 불변 객체. (팀 공용)
 *
 * <p>DB 설계서 규칙: {@code POINT(위도 경도)}, SRID 4326.
 * MySQL 8 은 SRID 4326 의 WKT 를 '위도 경도' 순서로 해석하므로 설계서와 그대로 일치한다.
 * <ul>
 *   <li>저장: native 쿼리에서 {@link #sqlFromWkt(int)} + {@link #toWkt()}</li>
 *   <li>조회: {@code ST_Latitude(col)}, {@code ST_Longitude(col)}</li>
 * </ul>
 * JPA 표준 타입에 POINT 가 없어서, 좌표 컬럼은 엔티티에 매핑하지 않고 native 쿼리로만 다룬다.
 */
public final class GeoPoint implements Serializable {

    private static final long serialVersionUID = 1L;

    private final double latitude;
    private final double longitude;

    private GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static GeoPoint of(double latitude, double longitude) {
        if (!isValid(latitude, longitude)) {
            throw new IllegalArgumentException("잘못된 좌표: " + latitude + ", " + longitude);
        }
        return new GeoPoint(latitude, longitude);
    }

    public static boolean isValid(double latitude, double longitude) {
        return Double.isFinite(latitude) && Double.isFinite(longitude)
                && latitude >= -90 && latitude <= 90
                && longitude >= -180 && longitude <= 180;
    }

    /** native 쿼리용 SQL 조각. 예: sqlFromWkt(2) → "ST_GeomFromText(?2, 4326)" */
    public static String sqlFromWkt(int paramIndex) {
        return "ST_GeomFromText(?" + paramIndex + ", 4326)";
    }

    /** WKT, 설계서 규칙대로 '위도 경도' 순서 */
    public String toWkt() {
        return String.format(Locale.ROOT, "POINT(%.7f %.7f)", latitude, longitude);
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return "GeoPoint{lat=" + latitude + ", lng=" + longitude + "}";
    }
}
