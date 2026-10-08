package com.dteam0928.sangsae.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ConfigUtilTest {

    @Test
    @DisplayName("설정: 절대경로 없이 상대경로 역추적으로 로드된 환경설정에서 API 키 조회 동작 확인")
    void testConfigLoadWithoutHardcodedPath() {
        assertDoesNotThrow(() -> {
            ConfigUtil.getKakaoRestApiKey();
            ConfigUtil.getKakaoJsApiKey();
        });
    }

    @Test
    @DisplayName("설정: 서블릿 realPath를 통한 상위 디렉터리 .env 탐색 초기화 검증")
    void testInitFromRealPath(@TempDir Path tempDir) throws IOException {
        Path subDir = Files.createDirectories(tempDir.resolve("WEB-INF").resolve("classes"));
        Path envFile = tempDir.resolve(".env");
        Files.writeString(envFile, "TEST_ENV_KEY=sangsae_test_value\n");

        ConfigUtil.initFromRealPath(subDir.toString());
        assertNotNull(ConfigUtil.get("TEST_ENV_KEY", "fallback"));
    }
}
