package com.dteam0928.sangsae.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public final class ConfigUtil {

    private static final Map<String, String> ENV_CACHE = new HashMap<>();
    private static final Properties CLASSPATH_PROPERTIES = new Properties();
    private static boolean envLoaded = false;

    static {
        try (InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream("kakao.properties")) {
            if (is != null) {
                CLASSPATH_PROPERTIES.load(is);
            }
        } catch (Exception ignored) {
        }

        loadDotEnv();
    }

    private ConfigUtil() {}

    public static synchronized void initFromRealPath(String realPath) {
        if (realPath != null && !realPath.isBlank()) {
            findAndParseDotEnv(new File(realPath));
        }
    }

    private static synchronized void loadDotEnv() {
        if (envLoaded && !ENV_CACHE.isEmpty()) {
            return;
        }

        List<File> candidates = new ArrayList<>();
        candidates.add(new File("C:/Dev/Sangsae/.env"));
        candidates.add(new File("C:\\Dev\\Sangsae\\.env"));
        candidates.add(new File("C:/Jsp/SangSae/.env"));

        // Tomcat 배포 구동 시 user.dir이 프로젝트 루트가 아닌 CATALINA_BASE/bin을 가리키므로,
        // 로컬 실행 및 다양한 배포 환경에서 프로젝트 루트 .env를 정상 감지하기 위해 상위 디렉터리 역추적 탐색 수행.
        String userDir = System.getProperty("user.dir");
        if (userDir != null) {
            File current = new File(userDir);
            for (int i = 0; i < 5 && current != null; i++) {
                candidates.add(new File(current, ".env"));
                current = current.getParentFile();
            }
        }

        try {
            URL location = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                File current = new File(location.toURI());
                for (int i = 0; i < 7 && current != null; i++) {
                    candidates.add(new File(current, ".env"));
                    current = current.getParentFile();
                }
            }
        } catch (Exception ignored) {
        }

        for (File file : candidates) {
            if (parseFile(file)) {
                envLoaded = true;
                break;
            }
        }
    }

    private static void findAndParseDotEnv(File startDir) {
        File current = startDir;
        for (int i = 0; i < 7 && current != null; i++) {
            File envFile = new File(current, ".env");
            if (parseFile(envFile)) {
                envLoaded = true;
                break;
            }
            current = current.getParentFile();
        }
    }

    private static boolean parseFile(File file) {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Windows 텍스트 에디터(메모장 등)가 파일 선두에 UTF-8 BOM(\uFEFF)을 삽입하여 첫 번째 키 매칭이 실패하는 현상 방지.
                if (line.startsWith("\uFEFF")) {
                    line = line.substring(1).trim();
                }

                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                int eqIdx = line.indexOf('=');
                if (eqIdx > 0) {
                    String key = line.substring(0, eqIdx).trim();
                    String val = line.substring(eqIdx + 1).trim();

                    if (val.startsWith("\"") && val.endsWith("\"") && val.length() >= 2) {
                        val = val.substring(1, val.length() - 1);
                    } else if (val.startsWith("'") && val.endsWith("'") && val.length() >= 2) {
                        val = val.substring(1, val.length() - 1);
                    }

                    ENV_CACHE.putIfAbsent(key, val);
                }
            }
            System.out.println("[SangSae ConfigUtil] .env loaded from: " + file.getAbsolutePath());
            return true;
        } catch (Exception e) {
            System.err.println("[SangSae ConfigUtil] Failed to parse .env (" + file.getAbsolutePath() + "): " + e.getMessage());
            return false;
        }
    }

    public static String get(String key) {
        if (key == null) return null;

        String val = System.getenv(key);
        if (val != null && !val.isBlank()) return val.trim();

        val = System.getProperty(key);
        if (val != null && !val.isBlank()) return val.trim();

        val = ENV_CACHE.get(key);
        if (val != null && !val.isBlank()) return val.trim();

        val = CLASSPATH_PROPERTIES.getProperty(key);
        if (val != null && !val.isBlank()) return val.trim();

        return null;
    }

    public static String get(String key, String defaultValue) {
        String val = get(key);
        return val != null ? val : defaultValue;
    }

    public static String getKakaoRestApiKey() {
        String[] keyCandidates = {
            "KAKAO_REST_API_KEY",
            "kakao.rest.api.key",
            "KAKAO_API_KEY",
            "kakao.api.key",
            "KAKAO_MAP_REST_API_KEY",
            "KAKAO_MAP_KEY",
            "kakao.map.key",
            "KAKAO_CLIENT_ID",
            "REST_API_KEY",
            "KAKAO_KEY"
        };

        for (String candidate : keyCandidates) {
            String val = get(candidate);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }

        return null;
    }

    public static String getKakaoJsApiKey() {
        String[] keyCandidates = {
            "KAKAO_JAVASCRIPT_KEY",
            "KAKAO_JS_KEY",
            "KAKAO_JS_API_KEY",
            "kakao.javascript.key",
            "kakao.js.key"
        };

        for (String candidate : keyCandidates) {
            String val = get(candidate);
            if (val != null && !val.isBlank()) {
                return val.trim();
            }
        }

        return null;
    }
}
