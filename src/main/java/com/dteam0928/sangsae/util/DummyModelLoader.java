package com.dteam0928.sangsae.util;

import jakarta.servlet.ServletContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * /resources/js/dummy.js 의 HOME_MODEL 구간을 request 모델로 로드합니다.
 */
public final class DummyModelLoader {
    private static final String RESOURCE_PATH = "/resources/js/dummy.js";
    private static final String START_MARKER = "/* HOME_MODEL_START */";
    private static final String END_MARKER = "/* HOME_MODEL_END */";

    private DummyModelLoader() {
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> loadHomeModel(ServletContext application) throws IOException {
        InputStream input = application.getResourceAsStream(RESOURCE_PATH);

        if (input == null) {
            throw new IllegalStateException("Dummy resource not found: " + RESOURCE_PATH);
        }

        StringBuilder source = new StringBuilder();

        try (Reader reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            char[] buffer = new char[4096];
            int count;

            while ((count = reader.read(buffer)) != -1) {
                source.append(buffer, 0, count);
            }
        }

        String text = source.toString();
        int start = text.indexOf(START_MARKER);

        if (start < 0) {
            throw new IllegalStateException("Dummy JSON start marker missing");
        }

        start += START_MARKER.length();
        int end = text.indexOf(END_MARKER, start);

        if (end < 0) {
            throw new IllegalStateException("Dummy JSON end marker missing");
        }

        Object parsed = new SimpleJsonReader(text.substring(start, end).trim()).parse();

        if (!(parsed instanceof Map)) {
            throw new IllegalStateException("homeModel must be an object");
        }

        return (Map<String, Object>) parsed;
    }
}
