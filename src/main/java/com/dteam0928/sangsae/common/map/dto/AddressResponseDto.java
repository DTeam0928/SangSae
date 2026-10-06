package com.dteam0928.sangsae.common.map.dto;

import java.io.Serializable;

public record AddressResponseDto(
    String roadAddress,
    String address,
    String region1Depth,
    String region2Depth,
    String region3Depth,
    String zoneNo,
    String buildingName,
    double latitude,
    double longitude
) implements Serializable {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String roadAddress = "";
        private String address = "";
        private String region1Depth = "";
        private String region2Depth = "";
        private String region3Depth = "";
        private String zoneNo = "";
        private String buildingName = "";
        private double latitude;
        private double longitude;

        public Builder roadAddress(String roadAddress) {
            this.roadAddress = roadAddress != null ? roadAddress : "";
            return this;
        }

        public Builder address(String address) {
            this.address = address != null ? address : "";
            return this;
        }

        public Builder regions(String r1, String r2, String r3) {
            this.region1Depth = r1 != null ? r1 : "";
            this.region2Depth = r2 != null ? r2 : "";
            this.region3Depth = r3 != null ? r3 : "";
            return this;
        }

        public Builder zoneNo(String zoneNo) {
            this.zoneNo = zoneNo != null ? zoneNo : "";
            return this;
        }

        public Builder buildingName(String buildingName) {
            this.buildingName = buildingName != null ? buildingName : "";
            return this;
        }

        public Builder coordinates(double lat, double lng) {
            this.latitude = lat;
            this.longitude = lng;
            return this;
        }

        public AddressResponseDto build() {
            return new AddressResponseDto(
                roadAddress,
                address,
                region1Depth,
                region2Depth,
                region3Depth,
                zoneNo,
                buildingName,
                latitude,
                longitude
            );
        }
    }
}
