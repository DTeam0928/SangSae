package com.dteam0928.sangsae.common.map.service;

import com.dteam0928.sangsae.common.map.dao.PlaceDao;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;

import java.sql.SQLException;
import java.util.List;

public class PlaceService {

    private final PlaceDao placeDao;

    public PlaceService() {
        this(new PlaceDao());
    }

    public PlaceService(PlaceDao placeDao) {
        this.placeDao = placeDao;
    }

    // 장소 선택 확정 시 DB places 테이블에 중복 방지(Get-or-Create) 영속화를 수행하고 외래키 연동용 PK를 반환.
    public Long getOrCreatePlace(PlaceResponseDto placeDto) {
        if (placeDto == null) {
            throw new IllegalArgumentException("장소 정보가 제공되지 않았습니다.");
        }
        if (placeDto.name() == null || placeDto.name().isBlank()) {
            throw new IllegalArgumentException("장소명은 필수입니다.");
        }
        if (placeDto.latitude() == 0.0 || placeDto.longitude() == 0.0) {
            throw new IllegalArgumentException("유효한 좌표(위도, 경도)가 필요합니다.");
        }

        try {
            return placeDao.getOrCreate(placeDto);
        } catch (SQLException e) {
            throw new RuntimeException("장소 저장 및 조회 처리 중 데이터베이스 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public PlaceResponseDto getPlaceById(Long placeId) {
        if (placeId == null || placeId <= 0) {
            return null;
        }

        try {
            return placeDao.findById(placeId);
        } catch (SQLException e) {
            throw new RuntimeException("장소 단건 조회 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    public List<PlaceResponseDto> findNearbyPlaces(double lat, double lng, int radiusInMeters) {
        int safeRadius = Math.min(Math.max(radiusInMeters, 100), 20000);
        try {
            return placeDao.findNearby(lat, lng, safeRadius);
        } catch (SQLException e) {
            throw new RuntimeException("반경 내 장소 검색 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}
