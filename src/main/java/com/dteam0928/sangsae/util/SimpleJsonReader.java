package com.dteam0928.sangsae.util;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 외부 JSON 라이브러리 없이 로컬 더미 JSON을 Map/List로 변환합니다.
 */
public final class SimpleJsonReader {
    private final String source;
    private int position;

    public SimpleJsonReader(String source) {
        this.source = source;
    }

    public Object parse() {
        Object result = readValue();
        skipWhitespace();

        if (position != source.length()) {
            throw error("Unexpected trailing content");
        }

        return result;
    }

    private IllegalArgumentException error(String message) {
        return new IllegalArgumentException(message + " at position " + position);
    }

    private void skipWhitespace() {
        while (position < source.length()) {
            char ch = source.charAt(position);

            if (ch != ' ' && ch != '\t' && ch != '\n' && ch != '\r') {
                break;
            }

            position++;
        }
    }

    private boolean consume(char expected) {
        skipWhitespace();

        if (position < source.length() && source.charAt(position) == expected) {
            position++;
            return true;
        }

        return false;
    }

    private void expect(char expected) {
        if (!consume(expected)) {
            throw error("Expected " + expected);
        }
    }

    private Object readValue() {
        skipWhitespace();

        if (position >= source.length()) {
            throw error("Unexpected end");
        }

        char ch = source.charAt(position);

        if (ch == '{') {
            return readObject();
        }
        if (ch == '[') {
            return readArray();
        }
        if (ch == '"') {
            return readString();
        }
        if (source.startsWith("true", position)) {
            position += 4;
            return Boolean.TRUE;
        }
        if (source.startsWith("false", position)) {
            position += 5;
            return Boolean.FALSE;
        }
        if (source.startsWith("null", position)) {
            position += 4;
            return null;
        }

        return readNumber();
    }

    private Map<String, Object> readObject() {
        expect('{');
        Map<String, Object> result = new LinkedHashMap<>();

        if (consume('}')) {
            return result;
        }

        do {
            String key = readString();
            expect(':');

            if (result.containsKey(key)) {
                throw error("Duplicate object key");
            }

            result.put(key, readValue());
        } while (consume(','));

        expect('}');
        return result;
    }

    private List<Object> readArray() {
        expect('[');
        List<Object> result = new ArrayList<>();

        if (consume(']')) {
            return result;
        }

        do {
            result.add(readValue());
        } while (consume(','));

        expect(']');
        return result;
    }

    private String readString() {
        expect('"');
        StringBuilder result = new StringBuilder();

        while (position < source.length()) {
            char ch = source.charAt(position++);

            if (ch == '"') {
                return result.toString();
            }
            if (ch < 0x20) {
                throw error("Invalid control character");
            }
            if (ch != '\\') {
                result.append(ch);
                continue;
            }
            if (position >= source.length()) {
                throw error("Incomplete escape");
            }

            char escaped = source.charAt(position++);

            switch (escaped) {
                case '"':
                    result.append('"');
                    break;
                case '\\':
                    result.append('\\');
                    break;
                case '/':
                    result.append('/');
                    break;
                case 'b':
                    result.append('\b');
                    break;
                case 'f':
                    result.append('\f');
                    break;
                case 'n':
                    result.append('\n');
                    break;
                case 'r':
                    result.append('\r');
                    break;
                case 't':
                    result.append('\t');
                    break;
                case 'u':
                    if (position + 4 > source.length()) {
                        throw error("Incomplete unicode escape");
                    }

                    String hex = source.substring(position, position + 4);

                    if (!hex.matches("[0-9a-fA-F]{4}")) {
                        throw error("Invalid unicode escape");
                    }

                    result.append((char) Integer.parseInt(hex, 16));
                    position += 4;
                    break;
                default:
                    throw error("Invalid escape");
            }
        }

        throw error("Unterminated string");
    }

    private Number readNumber() {
        int start = position;

        while (position < source.length()) {
            char ch = source.charAt(position);

            if (ch == ',' || ch == ']' || ch == '}' || Character.isWhitespace(ch)) {
                break;
            }

            position++;
        }

        String token = source.substring(start, position);

        if (!token.matches("-?(0|[1-9][0-9]*)(\\.[0-9]+)?([eE][+-]?[0-9]+)?")) {
            throw error("Invalid number");
        }

        return new BigDecimal(token);
    }
}
