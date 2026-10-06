package com.dteam0928.sangsae.auth.oauth;

import com.fasterxml.jackson.databind.JsonNode;
import com.dteam0928.sangsae.common.util.JsonUtil;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

/** 제공자 구현 공통: HTTP 호출, 쿼리스트링 조립 */
public abstract class AbstractOAuthProvider implements OAuthProvider {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(TIMEOUT).build();

    protected final OAuthClientConfig config;

    protected AbstractOAuthProvider(OAuthClientConfig config) {
        this.config = config;
    }

    /** a=1&b=2 (URL 인코딩, null 값은 제외) */
    protected static String encode(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> URLEncoder.encode(e.getKey(), StandardCharsets.UTF_8) + "="
                        + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }

    protected static Map<String, String> params(String... keyValues) {
        Map<String, String> map = new LinkedHashMap<>();
        for (int i = 0; i + 1 < keyValues.length; i += 2) map.put(keyValues[i], keyValues[i + 1]);
        return map;
    }

    /** application/x-www-form-urlencoded POST → JSON */
    protected JsonNode postForm(String url, Map<String, String> form, Map<String, String> headers) {
        HttpRequest.Builder b = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(encode(form)));
        headers.forEach(b::header);
        return send(b.build());
    }

    /** Bearer 토큰으로 GET → JSON */
    protected JsonNode getJson(String url, String accessToken) {
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .timeout(TIMEOUT)
                .header("Authorization", "Bearer " + accessToken)
                .header("Accept", "application/json")
                .GET().build();
        return send(req);
    }

    private JsonNode send(HttpRequest req) {
        try {
            HttpResponse<String> resp = HTTP.send(req, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            if (resp.statusCode() / 100 != 2) {
                throw new OAuthException(type() + " 응답 오류 " + resp.statusCode() + " (" + req.uri() + "): "
                        + truncate(resp.body()));
            }
            return JsonUtil.readTree(resp.body());
        } catch (IOException e) {
            throw new OAuthException(type() + " 통신 실패: " + req.uri(), e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new OAuthException(type() + " 요청이 중단되었습니다.", e);
        }
    }

    protected static String requireText(JsonNode node, String field) {
        String v = node.path(field).asText(null);
        if (v == null || v.isBlank()) throw new OAuthException("응답에 " + field + " 가 없습니다.");
        return v;
    }

    protected static String textOrNull(JsonNode node, String field) {
        String v = node.path(field).asText(null);
        return (v == null || v.isBlank()) ? null : v;
    }

    private static String truncate(String s) {
        return s == null ? "" : (s.length() > 300 ? s.substring(0, 300) + "..." : s);
    }
}
