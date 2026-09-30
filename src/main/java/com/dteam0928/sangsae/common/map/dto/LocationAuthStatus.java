package com.dteam0928.sangsae.common.map.dto;

public enum LocationAuthStatus {
    SUCCESS("인증 성공"),
    DISTANCE_EXCEEDED("허용 반경 초과"),
    PLACE_NOT_FOUND("등록된 장소를 찾을 수 없음"),
    INVALID_COORDINATES("유효하지 않은 GPS 좌표");

    private final String description;

    LocationAuthStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
