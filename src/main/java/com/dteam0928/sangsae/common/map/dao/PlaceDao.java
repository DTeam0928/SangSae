package com.dteam0928.sangsae.common.map.dao;

import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.dteam0928.sangsae.common.util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class PlaceDao {

    // 동일 장소 중복 저장 방지. 카카오 고유 ID로 기등록 여부를 조회하고, 존재하지 않을 때만 신규 생성하여 PK를 일관되게 반환(Get-or-Create 패턴).
    public Long getOrCreate(PlaceResponseDto dto) throws SQLException {
        if (dto == null) {
            throw new IllegalArgumentException("장소 데이터(PlaceResponseDto)가 null일 수 없습니다.");
        }

        if (dto.kakaoPlaceId() != null && !dto.kakaoPlaceId().isBlank()) {
            Long existingId = findIdByKakaoPlaceId(dto.kakaoPlaceId().trim());
            if (existingId != null) {
                return existingId;
            }
        }

        return insertPlace(dto);
    }

    public Long findIdByKakaoPlaceId(String kakaoPlaceId) throws SQLException {
        String sql = "SELECT id FROM places WHERE kakao_place_id = ?";
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, kakaoPlaceId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getLong("id");
                }
            }
        }
        return null;
    }

    public PlaceResponseDto findById(Long id) throws SQLException {
        // MySQL 8.0 SRID 4326 공간 좌표에서 ST_Latitude, ST_Longitude 함수로 위도/경도를 명시적 분리 추출.
        String sql = """
            SELECT id, kakao_place_id, name, road_address,
                   region_1depth_name, region_2depth_name, region_3depth_name,
                   ST_Latitude(coordinates) AS lat, ST_Longitude(coordinates) AS lng,
                   category
            FROM places
            WHERE id = ?
        """;

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setLong(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToDto(rs);
                }
            }
        }
        return null;
    }

    public List<PlaceResponseDto> findNearby(double lat, double lng, int radiusInMeters) throws SQLException {
        // MySQL SPATIAL 인덱스를 활용하여 사용자 기준 반경 내 장소를 고속 탐색하는 구면 거리(ST_Distance_Sphere) 쿼리.
        String sql = """
            SELECT id, kakao_place_id, name, road_address,
                   region_1depth_name, region_2depth_name, region_3depth_name,
                   ST_Latitude(coordinates) AS lat, ST_Longitude(coordinates) AS lng,
                   category,
                   ROUND(ST_Distance_Sphere(coordinates, ST_GeomFromText(?, 4326))) AS distance_meters
            FROM places
            WHERE ST_Distance_Sphere(coordinates, ST_GeomFromText(?, 4326)) <= ?
            ORDER BY distance_meters ASC
        """;

        String pointWkt = String.format("POINT(%.6f %.6f)", lat, lng);
        List<PlaceResponseDto> result = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, pointWkt);
            pstmt.setString(2, pointWkt);
            pstmt.setInt(3, radiusInMeters);

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    PlaceResponseDto dto = mapRowToDto(rs);
                    int dist = rs.getInt("distance_meters");
                    result.add(PlaceResponseDto.builder()
                        .kakaoPlaceId(dto.kakaoPlaceId())
                        .name(dto.name())
                        .roadAddress(dto.roadAddress())
                        .address(dto.address())
                        .regions(dto.region1Depth(), dto.region2Depth(), dto.region3Depth())
                        .coordinates(dto.latitude(), dto.longitude())
                        .theme(dto.themeCode(), dto.themeName())
                        .originalCategoryName(dto.originalCategoryName())
                        .distanceInMeters(dist)
                        .phone(dto.phone())
                        .placeUrl(dto.placeUrl())
                        .build());
                }
            }
        }
        return result;
    }

    private Long insertPlace(PlaceResponseDto dto) throws SQLException {
        String sql = """
            INSERT INTO places (
                kakao_place_id, name, road_address,
                region_1depth_name, region_2depth_name, region_3depth_name,
                coordinates, category
            ) VALUES (?, ?, ?, ?, ?, ?, ST_GeomFromText(?, 4326), ?)
        """;

        String pointWkt = String.format("POINT(%.6f %.6f)", dto.latitude(), dto.longitude());

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, dto.kakaoPlaceId());
            pstmt.setString(2, dto.name());
            pstmt.setString(3, dto.roadAddress());
            pstmt.setString(4, dto.region1Depth() != null && !dto.region1Depth().isBlank() ? dto.region1Depth() : "기타");
            pstmt.setString(5, dto.region2Depth() != null && !dto.region2Depth().isBlank() ? dto.region2Depth() : "기타");
            pstmt.setString(6, dto.region3Depth() != null && !dto.region3Depth().isBlank() ? dto.region3Depth() : "기타");
            pstmt.setString(7, pointWkt);
            pstmt.setString(8, dto.themeCode() != null ? dto.themeCode() : "WALK");

            pstmt.executeUpdate();

            try (ResultSet keys = pstmt.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getLong(1);
                }
            }
        }

        throw new SQLException("장소 등록 후 자동 생성된 PK(id)를 조회하지 못했습니다.");
    }

    private PlaceResponseDto mapRowToDto(ResultSet rs) throws SQLException {
        String themeCode = rs.getString("category");
        SangsaeTheme theme = SangsaeTheme.WALK;
        if (themeCode != null) {
            try {
                theme = SangsaeTheme.valueOf(themeCode.toUpperCase());
            } catch (IllegalArgumentException ignored) {}
        }

        return PlaceResponseDto.builder()
            .kakaoPlaceId(rs.getString("kakao_place_id"))
            .name(rs.getString("name"))
            .roadAddress(rs.getString("road_address"))
            .address(rs.getString("road_address"))
            .regions(
                rs.getString("region_1depth_name"),
                rs.getString("region_2depth_name"),
                rs.getString("region_3depth_name")
            )
            .coordinates(rs.getDouble("lat"), rs.getDouble("lng"))
            .theme(theme.getCode(), theme.getDisplayName())
            .originalCategoryName(theme.getDisplayName())
            .build();
    }
}
