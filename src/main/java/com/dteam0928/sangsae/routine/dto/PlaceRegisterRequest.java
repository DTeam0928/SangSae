package com.dteam0928.sangsae.routine.dto;

/**
 * 내가 다니는 장소 등록 요청.
 * <ul>
 *   <li>이미 places 에 있는 장소: placeId 만 보낸다</li>
 *   <li>카카오 검색 결과로 새 장소: kakaoPlaceId, name, latitude(카카오 y), longitude(카카오 x) 필수.
 *       region1~3 이 없으면 addressName("부산 부산진구 가야동 24")을 앞에서부터 잘라 채운다</li>
 * </ul>
 */
public class PlaceRegisterRequest {

    private Long placeId;
    private String kakaoPlaceId;
    private String name;
    private String roadAddress;
    private String addressName;
    private String region1;
    private String region2;
    private String region3;
    private Double latitude;
    private Double longitude;
    private String category;

    public Long getPlaceId() { return placeId; }
    public void setPlaceId(Long placeId) { this.placeId = placeId; }
    public String getKakaoPlaceId() { return kakaoPlaceId; }
    public void setKakaoPlaceId(String kakaoPlaceId) { this.kakaoPlaceId = kakaoPlaceId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getRoadAddress() { return roadAddress; }
    public void setRoadAddress(String roadAddress) { this.roadAddress = roadAddress; }
    public String getAddressName() { return addressName; }
    public void setAddressName(String addressName) { this.addressName = addressName; }
    public String getRegion1() { return region1; }
    public void setRegion1(String region1) { this.region1 = region1; }
    public String getRegion2() { return region2; }
    public void setRegion2(String region2) { this.region2 = region2; }
    public String getRegion3() { return region3; }
    public void setRegion3(String region3) { this.region3 = region3; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
