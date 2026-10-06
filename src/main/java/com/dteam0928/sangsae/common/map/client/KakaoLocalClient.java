package com.dteam0928.sangsae.common.map.client;

import com.dteam0928.sangsae.common.util.ConfigUtil;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class KakaoLocalClient {

    private static final String BASE_URL = "https://dapi.kakao.com/v2/local";
    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final HttpClient httpClient;

    public KakaoLocalClient() {
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(TIMEOUT)
                .build();
    }

    private String getAuthorizationHeader() {
        String apiKey = ConfigUtil.getKakaoRestApiKey();
        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException("카카오 REST API 키가 설정되지 않았습니다. .env 파일 또는 kakao.properties 설정을 확인하세요.");
        }
        return "KakaoAK " + apiKey.trim();
    }

    public JsonObject searchKeyword(String query, Double lat, Double lng, Integer radius, Integer page, Integer size)
            throws IOException, InterruptedException {
        if (query == null || query.isBlank()) {
            throw new IllegalArgumentException("검색어(query)는 필수입니다.");
        }

        StringBuilder urlBuilder = new StringBuilder(BASE_URL)
                .append("/search/keyword.json?query=")
                .append(URLEncoder.encode(query.trim(), StandardCharsets.UTF_8));

        if (lat != null && lng != null) {
            //카카오 로컬 API는 일반적인 (위도, 경도) 순서와 달리 x=경도(lng), y=위도(lat)로 정의하므로 파라미터 반전 매핑.
            urlBuilder.append("&x=").append(lng)
                      .append("&y=").append(lat);
            if (radius != null && radius > 0) {
                urlBuilder.append("&radius=").append(Math.min(radius, 20000));
            }
        }

        int pageNum = (page != null && page > 0) ? page : 1;
        int pageSize = (size != null && size > 0 && size <= 15) ? size : 15;
        urlBuilder.append("&page=").append(pageNum).append("&size=").append(pageSize);

        return executeGet(urlBuilder.toString());
    }

    public JsonObject searchAddress(String address, Integer page, Integer size)
            throws IOException, InterruptedException {
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("주소(address)는 필수입니다.");
        }

        int pageNum = (page != null && page > 0) ? page : 1;
        int pageSize = (size != null && size > 0 && size <= 30) ? size : 10;

        String url = BASE_URL + "/search/address.json?query="
                + URLEncoder.encode(address.trim(), StandardCharsets.UTF_8)
                + "&page=" + pageNum + "&size=" + pageSize;

        return executeGet(url);
    }

    public JsonObject coordToAddress(double lat, double lng)
            throws IOException, InterruptedException {
        String url = BASE_URL + "/geo/coord2address.json?x=" + lng + "&y=" + lat;
        return executeGet(url);
    }

    private JsonObject executeGet(String url) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", getAuthorizationHeader())
                .header("Accept", "application/json")
                .timeout(TIMEOUT)
                .GET()
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));

        int statusCode = response.statusCode();
        String responseBody = response.body();

        if (statusCode == 200) {
            return JsonParser.parseString(responseBody).getAsJsonObject();
        } else if (statusCode == 401) {
            throw new IllegalStateException("카카오 REST API 키 인증에 실패했습니다 (401 Unauthorized). 앱 키 및 플랫폼 설정을 확인하세요.");
        } else if (statusCode == 429) {
            throw new IllegalStateException("카카오 API 일일/월간 쿼터를 초과했습니다 (429 Too Many Requests).");
        } else {
            throw new IOException("카카오 로컬 API 요청 실패 [HTTP " + statusCode + "]: " + responseBody);
        }
    }
}
