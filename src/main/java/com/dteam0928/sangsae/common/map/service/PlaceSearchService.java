package com.dteam0928.sangsae.common.map.service;

import com.dteam0928.sangsae.common.map.client.KakaoLocalClient;
import com.dteam0928.sangsae.common.map.dto.AddressResponseDto;
import com.dteam0928.sangsae.common.map.dto.PlaceResponseDto;
import com.dteam0928.sangsae.common.map.theme.SangsaeTheme;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class PlaceSearchService {

    private final KakaoLocalClient kakaoClient;

    public PlaceSearchService() {
        this(new KakaoLocalClient());
    }

    public PlaceSearchService(KakaoLocalClient kakaoClient) {
        this.kakaoClient = kakaoClient;
    }

    public List<PlaceResponseDto> searchPlaces(String query, Double userLat, Double userLng, Integer radius) {
        List<PlaceResponseDto> resultList = new ArrayList<>();
        if (query == null || query.isBlank()) {
            return resultList;
        }

        try {
            JsonObject json = kakaoClient.searchKeyword(query, userLat, userLng, radius, 1, 15);
            if (!json.has("documents")) {
                return resultList;
            }

            JsonArray documents = json.getAsJsonArray("documents");
            for (JsonElement docElement : documents) {
                JsonObject doc = docElement.getAsJsonObject();

                String kakaoPlaceId = getAsString(doc, "id");
                String name = getAsString(doc, "place_name");
                String roadAddress = getAsString(doc, "road_address_name");
                String address = getAsString(doc, "address_name");
                String phone = getAsString(doc, "phone");
                String placeUrl = getAsString(doc, "place_url");

                double lng = parseDouble(getAsString(doc, "x"), 0.0);
                double lat = parseDouble(getAsString(doc, "y"), 0.0);

                String categoryGroupCode = getAsString(doc, "category_group_code");
                String originalCategory = getAsString(doc, "category_name");

                SangsaeTheme theme = SangsaeTheme.fromKakao(categoryGroupCode, originalCategory, name);
                String[] regions = parseAddressDepths(roadAddress, address);

                // 카카오 키워드 검색 결과가 특정 기준을 벗어나 거리를 누락할 때, Haversine 구면 삼각 공식으로 폴백 계산하여 퀘스트/루틴 거리순 정렬의 일관성 보장.
                Integer distance = null;
                String distanceStr = getAsString(doc, "distance");
                if (distanceStr != null && !distanceStr.isBlank()) {
                    try {
                        int parsedDist = Integer.parseInt(distanceStr);
                        if (parsedDist > 0) distance = parsedDist;
                    } catch (NumberFormatException ignored) {}
                }

                if (distance == null && userLat != null && userLng != null && lat != 0.0 && lng != 0.0) {
                    distance = calculateDistanceInMeters(userLat, userLng, lat, lng);
                }

                PlaceResponseDto dto = PlaceResponseDto.builder()
                        .kakaoPlaceId(kakaoPlaceId)
                        .name(name)
                        .roadAddress(roadAddress)
                        .address(address)
                        .regions(regions[0], regions[1], regions[2])
                        .coordinates(lat, lng)
                        .theme(theme.getCode(), theme.getDisplayName())
                        .originalCategoryName(originalCategory)
                        .distanceInMeters(distance)
                        .phone(phone)
                        .placeUrl(placeUrl)
                        .build();

                resultList.add(dto);
            }

            if (userLat != null && userLng != null) {
                resultList.sort(Comparator.comparing(
                    PlaceResponseDto::distanceInMeters,
                    Comparator.nullsLast(Integer::compareTo)
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("장소 검색 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return resultList;
    }

    public List<PlaceResponseDto> searchAddress(String addressQuery, Double userLat, Double userLng) {
        List<PlaceResponseDto> resultList = new ArrayList<>();
        if (addressQuery == null || addressQuery.isBlank()) {
            return resultList;
        }

        try {
            JsonObject json = kakaoClient.searchAddress(addressQuery, 1, 10);
            if (!json.has("documents")) {
                return resultList;
            }

            JsonArray documents = json.getAsJsonArray("documents");
            for (JsonElement docElement : documents) {
                JsonObject doc = docElement.getAsJsonObject();

                String addressName = getAsString(doc, "address_name");
                double lng = parseDouble(getAsString(doc, "x"), 0.0);
                double lat = parseDouble(getAsString(doc, "y"), 0.0);

                String roadAddress = "";
                String r1 = "";
                String r2 = "";
                String r3 = "";

                if (doc.has("road_address") && !doc.get("road_address").isJsonNull()) {
                    JsonObject roadObj = doc.getAsJsonObject("road_address");
                    roadAddress = getAsString(roadObj, "address_name");
                    r1 = getAsString(roadObj, "region_1depth_name");
                    r2 = getAsString(roadObj, "region_2depth_name");
                    r3 = getAsString(roadObj, "region_3depth_name");
                } else if (doc.has("address") && !doc.get("address").isJsonNull()) {
                    JsonObject addrObj = doc.getAsJsonObject("address");
                    r1 = getAsString(addrObj, "region_1depth_name");
                    r2 = getAsString(addrObj, "region_2depth_name");
                    r3 = getAsString(addrObj, "region_3depth_name");
                }

                if (r1 == null || r1.isBlank()) {
                    String[] parsed = parseAddressDepths(roadAddress, addressName);
                    r1 = parsed[0];
                    r2 = parsed[1];
                    r3 = parsed[2];
                }

                Integer distance = null;
                if (userLat != null && userLng != null && lat != 0.0 && lng != 0.0) {
                    distance = calculateDistanceInMeters(userLat, userLng, lat, lng);
                }

                PlaceResponseDto dto = PlaceResponseDto.builder()
                        .name(addressName)
                        .roadAddress(roadAddress)
                        .address(addressName)
                        .regions(r1, r2, r3)
                        .coordinates(lat, lng)
                        .theme(SangsaeTheme.WALK.getCode(), SangsaeTheme.WALK.getDisplayName())
                        .originalCategoryName("주소")
                        .distanceInMeters(distance)
                        .build();

                resultList.add(dto);
            }

        } catch (Exception e) {
            throw new RuntimeException("주소 검색 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }

        return resultList;
    }

    public AddressResponseDto coordToAddress(double lat, double lng) {
        if (lat < -90.0 || lat > 90.0 || lng < -180.0 || lng > 180.0) {
            throw new IllegalArgumentException("유효하지 않은 위도/경도 좌표 범위입니다.");
        }

        try {
            JsonObject json = kakaoClient.coordToAddress(lat, lng);
            if (!json.has("documents")) {
                return null;
            }

            JsonArray documents = json.getAsJsonArray("documents");
            if (documents.isEmpty()) {
                return null;
            }

            JsonObject doc = documents.get(0).getAsJsonObject();
            String roadAddress = "";
            String r1 = "";
            String r2 = "";
            String r3 = "";
            String zoneNo = "";
            String buildingName = "";

            if (doc.has("road_address") && !doc.get("road_address").isJsonNull()) {
                JsonObject roadObj = doc.getAsJsonObject("road_address");
                roadAddress = getAsString(roadObj, "address_name");
                r1 = getAsString(roadObj, "region_1depth_name");
                r2 = getAsString(roadObj, "region_2depth_name");
                r3 = getAsString(roadObj, "region_3depth_name");
                zoneNo = getAsString(roadObj, "zone_no");
                buildingName = getAsString(roadObj, "building_name");
            }

            String address = "";
            if (doc.has("address") && !doc.get("address").isJsonNull()) {
                JsonObject addrObj = doc.getAsJsonObject("address");
                address = getAsString(addrObj, "address_name");
                if (r1.isBlank()) r1 = getAsString(addrObj, "region_1depth_name");
                if (r2.isBlank()) r2 = getAsString(addrObj, "region_2depth_name");
                if (r3.isBlank()) r3 = getAsString(addrObj, "region_3depth_name");
            }

            if (r1.isBlank()) {
                String[] parsed = parseAddressDepths(roadAddress, address);
                r1 = parsed[0];
                r2 = parsed[1];
                r3 = parsed[2];
            }

            return AddressResponseDto.builder()
                    .roadAddress(roadAddress)
                    .address(address)
                    .regions(r1, r2, r3)
                    .zoneNo(zoneNo)
                    .buildingName(buildingName)
                    .coordinates(lat, lng)
                    .build();

        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("좌표 기반 주소 역변환 처리 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    private String[] parseAddressDepths(String roadAddress, String address) {
        String base = (roadAddress != null && !roadAddress.isBlank()) ? roadAddress : address;
        if (base == null || base.isBlank()) {
            return new String[]{"", "", ""};
        }

        String[] tokens = base.split("\\s+");
        String r1 = tokens.length > 0 ? tokens[0] : "";
        String r2 = tokens.length > 1 ? tokens[1] : "";
        String r3 = tokens.length > 2 ? tokens[2] : "";
        return new String[]{r1, r2, r3};
    }

    public static int calculateDistanceInMeters(double lat1, double lon1, double lat2, double lon2) {
        final double EARTH_RADIUS_METERS = 6371000.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return (int) Math.round(EARTH_RADIUS_METERS * c);
    }

    private String getAsString(JsonObject obj, String memberName) {
        if (obj.has(memberName) && !obj.get(memberName).isJsonNull()) {
            return obj.get(memberName).getAsString();
        }
        return "";
    }

    private double parseDouble(String str, double defaultVal) {
        try {
            return Double.parseDouble(str);
        } catch (Exception ignored) {
            return defaultVal;
        }
    }
}
