package com.dteam0928.sangsae.common.map.dto;

import java.io.Serializable;

public record LocationAuthResult(
    boolean isSuccess,
    LocationAuthStatus status,
    int distanceInMeters,
    int allowedRadiusMeters,
    String message,
    PlaceResponseDto place
) implements Serializable {

    public static LocationAuthResult success(int distanceInMeters, int allowedRadiusMeters, PlaceResponseDto place) {
        String msg = String.format("위치 인증에 성공했습니다. (목표 지점과의 거리: %dm)", distanceInMeters);
        return new LocationAuthResult(true, LocationAuthStatus.SUCCESS, distanceInMeters, allowedRadiusMeters, msg, place);
    }

    public static LocationAuthResult distanceExceeded(int distanceInMeters, int allowedRadiusMeters, PlaceResponseDto place) {
        String msg = String.format(
            "지정된 장소 반경 %dm 이내에서만 인증할 수 있습니다. (현재 거리: %dm)",
            allowedRadiusMeters, distanceInMeters
        );
        return new LocationAuthResult(false, LocationAuthStatus.DISTANCE_EXCEEDED, distanceInMeters, allowedRadiusMeters, msg, place);
    }

    public static LocationAuthResult placeNotFound(Long placeId) {
        String msg = String.format("인증 대상 장소(ID: %s)를 찾을 수 없습니다.", placeId != null ? placeId : "null");
        return new LocationAuthResult(false, LocationAuthStatus.PLACE_NOT_FOUND, -1, 0, msg, null);
    }

    public static LocationAuthResult invalidCoordinates(String reason) {
        return new LocationAuthResult(false, LocationAuthStatus.INVALID_COORDINATES, -1, 0, reason, null);
    }
}
