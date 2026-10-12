package com.dteam0928.sangsae.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
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
        if (envLoaded && !ENV_CACHE.isEmpty()) {
            return;
        }
        if (realPath != null && !realPath.isBlank()) {
            if (searchUpwardForDotEnv(new File(realPath), 10)) {
                envLoaded = true;
            }
        }
    }

    private static synchronized void loadDotEnv() {
        if (envLoaded && !ENV_CACHE.isEmpty()) {
            return;
        }

        // 1. 현재 작업 경로 및 상위 디렉터리 상대경로 직접 탐색
        String[] relativePaths = {
            ".env",
            "../.env",
            "../../.env",
            "../../../.env",
            "../../../../.env"
        };
        for (String relPath : relativePaths) {
            File file = new File(relPath);
            if (parseFile(file)) {
                envLoaded = true;
                return;
            }
        }

        // 2. 시스템 프로퍼티 또는 환경변수로 명시적 경로가 지정된 경우 우선 탐색
        String explicitPath = System.getProperty("env.file");
        if (explicitPath == null || explicitPath.isBlank()) {
            explicitPath = System.getenv("ENV_PATH");
        }
        if (explicitPath == null || explicitPath.isBlank()) {
            explicitPath = System.getenv("DOTENV_PATH");
        }
        if (explicitPath != null && !explicitPath.isBlank()) {
            if (parseFile(new File(explicitPath.trim()))) {
                envLoaded = true;
                return;
            }
        }

        // 3. user.dir 기준 상위 디렉터리 역추적 탐색
        String userDir = System.getProperty("user.dir");
        if (userDir != null && searchUpwardForDotEnv(new File(userDir), 10)) {
            envLoaded = true;
            return;
        }

        // 4. CodeSource 위치(classes/JAR 디렉터리) 기준 상위 디렉터리 역추적 탐색
        try {
            URL location = ConfigUtil.class.getProtectionDomain().getCodeSource().getLocation();
            if (location != null) {
                File current = new File(location.toURI());
                if (searchUpwardForDotEnv(current, 10)) {
                    envLoaded = true;
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        // 5. 클래스패스 루트 URL 기준 상위 디렉터리 역추적 탐색
        try {
            URL resourceUrl = ConfigUtil.class.getResource("/");
            if (resourceUrl != null) {
                File current = new File(resourceUrl.toURI());
                if (searchUpwardForDotEnv(current, 10)) {
                    envLoaded = true;
                    return;
                }
            }
        } catch (Exception ignored) {
        }

        // 6. 클래스패스 리소스 스트림 폴백 (빌드 시 클래스패스에 .env가 복사되었거나 패키징된 경우 대응)
        InputStream is = ConfigUtil.class.getClassLoader().getResourceAsStream(".env");
        if (is == null) {
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(".env");
        }
        if (is != null && parseStream(is, "classpath:/.env")) {
            envLoaded = true;
        }
    }

    private static boolean searchUpwardForDotEnv(File start, int maxLevels) {
        if (start == null || !start.exists()) {
            return false;
        }

        File current = start.isDirectory() ? start : start.getParentFile();
        for (int i = 0; i < maxLevels && current != null; i++) {
            File envFile = new File(current, ".env");
            if (parseFile(envFile)) {
                return true;
            }

            // Eclipse WTP 배포 환경(.metadata/.../wtpwebapps) 감지 시 워크스페이스 루트 내 프로젝트 디렉터리 탐색
            if (".metadata".equalsIgnoreCase(current.getName()) && current.getParentFile() != null) {
                File workspaceRoot = current.getParentFile();
                if (parseFile(new File(workspaceRoot, "Sangsae/.env"))
                        || parseFile(new File(workspaceRoot, "sangsae/.env"))) {
                    return true;
                }
            }

            current = current.getParentFile();
        }
        return false;
    }

    private static boolean parseFile(File file) {
        if (file == null || !file.exists() || !file.isFile() || !file.canRead()) {
            return false;
        }

        try (InputStream is = new FileInputStream(file)) {
            return parseStream(is, file.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("[SangSae ConfigUtil] Failed to parse .env (" + file.getAbsolutePath() + "): " + e.getMessage());
            return false;
        }
    }

    private static boolean parseStream(InputStream is, String sourceDesc) {
        if (is == null) {
            return false;
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                // Windows 텍스트 에디터(메모장 등) 편집 시 선두에 삽입되는 UTF-8 BOM(\uFEFF) 제거
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
            System.out.println("[SangSae ConfigUtil] .env loaded from: " + sourceDesc);
            return true;
        } catch (Exception e) {
            System.err.println("[SangSae ConfigUtil] Failed to parse .env (" + sourceDesc + "): " + e.getMessage());
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
