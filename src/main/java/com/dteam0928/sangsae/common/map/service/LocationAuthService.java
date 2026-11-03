package com.dteam0928.sangsae.common.map.service;

import com.dteam0928.sangsae.common.map.dto.LocationAuthResult;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;

public class LocationAuthService {

    // 기획서 비즈니스 규칙 상 퀘스트/루틴 현장 방문 인증 기본 허용 반경 (50m)
    public static final int DEFAULT_ALLOWED_RADIUS_METERS = 50;

    private final PlaceService placeService;

    public LocationAuthService() {
        this(new PlaceService());
    }

    public LocationAuthService(PlaceService placeService) {
        this.placeService = placeService;
    }

    public LocationAuthResult verifyLocation(Long placeId, Double userLat, Double userLng) {
        return verifyLocation(placeId, userLat, userLng, DEFAULT_ALLOWED_RADIUS_METERS);
    }

    public LocationAuthResult verifyLocation(Long placeId, Double userLat, Double userLng, int allowedRadiusMeters) {
        String validationError = validateCoordinates(userLat, userLng);
        if (validationError != null) {
            return LocationAuthResult.invalidCoordinates(validationError);
        }

        if (placeId == null || placeId <= 0) {
            return LocationAuthResult.placeNotFound(placeId);
        }

        PlaceResponseDto place = placeService.getPlaceById(placeId);
        if (place == null) {
            return LocationAuthResult.placeNotFound(placeId);
        }

        return verifyLocation(place, userLat, userLng, allowedRadiusMeters);
    }

    public LocationAuthResult verifyLocation(PlaceResponseDto place, Double userLat, Double userLng, int allowedRadiusMeters) {
        if (place == null) {
            return LocationAuthResult.placeNotFound(null);
        }

        String validationError = validateCoordinates(userLat, userLng);
        if (validationError != null) {
            return LocationAuthResult.invalidCoordinates(validationError);
        }

        int distance = PlaceSearchService.calculateDistanceInMeters(
            userLat, userLng, place.latitude(), place.longitude()
        );

        if (distance <= allowedRadiusMeters) {
            return LocationAuthResult.success(distance, allowedRadiusMeters, place);
        } else {
            return LocationAuthResult.distanceExceeded(distance, allowedRadiusMeters, place);
        }
    }

    public LocationAuthResult verifyCoordinates(double targetLat, double targetLng, double userLat, double userLng, int allowedRadiusMeters) {
        String validationError = validateCoordinates(userLat, userLng);
        if (validationError != null) {
            return LocationAuthResult.invalidCoordinates(validationError);
        }

        String targetError = validateCoordinates(targetLat, targetLng);
        if (targetError != null) {
            return LocationAuthResult.invalidCoordinates("목표 장소의 좌표가 올바르지 않습니다: " + targetError);
        }

        int distance = PlaceSearchService.calculateDistanceInMeters(userLat, userLng, targetLat, targetLng);
        if (distance <= allowedRadiusMeters) {
            return LocationAuthResult.success(distance, allowedRadiusMeters, null);
        } else {
            return LocationAuthResult.distanceExceeded(distance, allowedRadiusMeters, null);
        }
    }

    public void validateOrThrow(Long placeId, Double userLat, Double userLng) {
        validateOrThrow(placeId, userLat, userLng, DEFAULT_ALLOWED_RADIUS_METERS);
    }

    public void validateOrThrow(Long placeId, Double userLat, Double userLng, int allowedRadiusMeters) {
        LocationAuthResult result = verifyLocation(placeId, userLat, userLng, allowedRadiusMeters);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.message());
        }
    }

    // 모바일 브라우저의 Geolocation 미수신(0.0)이나 위변조된 비정상 GPS 좌표 사전 차단
    private String validateCoordinates(Double lat, Double lng) {
        if (lat == null || lng == null) {
            return "사용자 현재 GPS 좌표가 누락되었습니다.";
        }
        if (lat == 0.0 && lng == 0.0) {
            return "유효하지 않은 좌표(0.0, 0.0)입니다. 위치 권한을 확인해주세요.";
        }
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            return "위경도 좌표 범위가 비정상적입니다.";
        }
        return null;
    }
}
