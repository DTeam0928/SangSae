package com.dteam0928.sangsae.common.map.dto;

import java.io.Serializable;

public record PlaceResponseDto(
    Long placeId,
    String kakaoPlaceId,
    String name,
    String roadAddress,
    String address,
    String region1Depth,
    String region2Depth,
    String region3Depth,
    double latitude,
    double longitude,
    String themeCode,
    String themeName,
    String originalCategoryName,
    Integer distanceInMeters,
    String phone,
    String placeUrl
) implements Serializable {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Long placeId;
        private String kakaoPlaceId;
        private String name;
        private String roadAddress;
        private String address;
        private String region1Depth = "";
        private String region2Depth = "";
        private String region3Depth = "";
        private double latitude;
        private double longitude;
        private String themeCode;
        private String themeName;
        private String originalCategoryName;
        private Integer distanceInMeters;
        private String phone;
        private String placeUrl;

        public Builder kakaoPlaceId(String kakaoPlaceId) {
            this.kakaoPlaceId = kakaoPlaceId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder roadAddress(String roadAddress) {
            this.roadAddress = roadAddress;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Builder regions(String r1, String r2, String r3) {
            this.region1Depth = r1 != null ? r1 : "";
            this.region2Depth = r2 != null ? r2 : "";
            this.region3Depth = r3 != null ? r3 : "";
            return this;
        }

        public Builder coordinates(double lat, double lng) {
            this.latitude = lat;
            this.longitude = lng;
            return this;
        }

        public Builder theme(String themeCode, String themeName) {
            this.themeCode = themeCode;
            this.themeName = themeName;
            return this;
        }

        public Builder originalCategoryName(String originalCategoryName) {
            this.originalCategoryName = originalCategoryName;
            return this;
        }

        public Builder distanceInMeters(Integer distanceInMeters) {
            this.distanceInMeters = distanceInMeters;
            return this;
        }

        public Builder placeId(Long placeId) {
            this.placeId = placeId;
            return this;
        }

        public Builder phone(String phone) {
            this.phone = phone;
            return this;
        }

        public Builder placeUrl(String placeUrl) {
            this.placeUrl = placeUrl;
            return this;
        }

        public PlaceResponseDto build() {
            return new PlaceResponseDto(
                placeId,
                kakaoPlaceId,
                name,
                roadAddress,
                address,
                region1Depth,
                region2Depth,
                region3Depth,
                latitude,
                longitude,
                themeCode,
                themeName,
                originalCategoryName,
                distanceInMeters,
                phone,
                placeUrl
            );
        }
    }
}
