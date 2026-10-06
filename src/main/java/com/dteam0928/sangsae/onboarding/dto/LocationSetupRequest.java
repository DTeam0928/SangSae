package com.dteam0928.sangsae.onboarding.dto;

/**
 * O-03 위치 권한 결과.
 * permitted=true 이면 위도/경도가 함께 와야 한다. 브라우저에서 거부/타임아웃이면 permitted=false.
 */
public class LocationSetupRequest {

    private boolean permitted;
    private Double latitude;
    private Double longitude;

    public LocationSetupRequest() {
    }

    public LocationSetupRequest(boolean permitted, Double latitude, Double longitude) {
        this.permitted = permitted;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public boolean isPermitted() { return permitted; }
    public void setPermitted(boolean permitted) { this.permitted = permitted; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
}
