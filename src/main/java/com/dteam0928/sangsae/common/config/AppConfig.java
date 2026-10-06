package com.dteam0928.sangsae.common.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

/**
 * 설정값 조회.
 *
 * <p>우선순위 (위가 이김)
 * <ol>
 *   <li>환경변수 : {@code db.password} → {@code SANGSAE_DB_PASSWORD}</li>
 *   <li>JVM 옵션 : {@code -Ddb.password=...}</li>
 *   <li>{@code application-local.properties} (git 에 올리지 않는 개인 설정, 시크릿 보관용)</li>
 *   <li>{@code application.properties} (팀 공용 기본값)</li>
 * </ol>
 */
public class AppConfig {

    private static final String DEFAULT_FILE = "application.properties";
    private static final String LOCAL_FILE = "application-local.properties";

    private final Properties props = new Properties();

    public AppConfig() {
        loadIfExists(DEFAULT_FILE);
        loadIfExists(LOCAL_FILE);
    }

    /** 테스트용: 주어진 값으로만 구성 */
    public AppConfig(Properties props) {
        this.props.putAll(props);
    }

    private void loadIfExists(String name) {
        try (InputStream in = Thread.currentThread().getContextClassLoader().getResourceAsStream(name)) {
            if (in == null) return;
            // Properties.load(InputStream) 은 ISO-8859-1 로 읽어서 한글이 깨지므로 Reader 로 읽는다
            props.load(new InputStreamReader(in, StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new IllegalStateException("설정 파일을 읽을 수 없습니다: " + name, e);
        }
    }

    public String get(String key) {
        String envKey = "SANGSAE_" + key.toUpperCase(Locale.ROOT).replace('.', '_').replace('-', '_');
        String v = System.getenv(envKey);
        if (isBlank(v)) v = System.getProperty(key);
        if (isBlank(v)) v = props.getProperty(key);
        return isBlank(v) ? null : v.trim();
    }

    public String get(String key, String defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : v;
    }

    public String require(String key) {
        String v = get(key);
        if (v == null) throw new IllegalStateException("필수 설정이 없습니다: " + key);
        return v;
    }

    public int getInt(String key, int defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : Integer.parseInt(v);
    }

    public boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        return v == null ? defaultValue : Boolean.parseBoolean(v);
    }

    /** 콤마 구분 목록 */
    public List<String> getList(String key, String defaultValue) {
        return Arrays.stream(get(key, defaultValue).split(","))
                .map(String::trim).filter(s -> !s.isEmpty()).toList();
    }

    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }
}
