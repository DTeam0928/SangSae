package com.dteam0928.sangsae.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

public class DBUtilTest {

    @Test
    @DisplayName("DB 스키마 초기화 및 연결 테스트")
    void testInitAndConnection() {
        String dbUser = ConfigUtil.get("DB_USER");
        if (dbUser == null || dbUser.isBlank()) {
            System.out.println("[DBUtilTest] .env에 DB_USER가 설정되지 않아 테스트를 스킵합니다.");
            return;
        }

        // 1. 데이터베이스 생성 및 schema.sql 테이블 초기화 실행
        boolean initSuccess = DBUtil.initDatabaseAndSchema();
        assertTrue(initSuccess, "DB 및 테이블 초기화가 성공해야 합니다.");

        // 2. HikariCP 커넥션 풀을 통한 연결 테스트
        boolean connectionSuccess = DBUtil.testConnection();
        assertTrue(connectionSuccess, "HikariCP를 통한 DB 연결이 성공해야 합니다.");

        // 3. 생성된 테이블 검증 (places, interest_categories)
        try (Connection conn = DBUtil.getConnection();
             Statement stmt = conn.createStatement()) {

            // interest_categories 8개 테마 확인
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM interest_categories")) {
                assertTrue(rs.next());
                int count = rs.getInt(1);
                assertEquals(8, count, "관심사 8대 테마가 8개 등록되어 있어야 합니다.");
                System.out.println("[DBUtilTest] interest_categories 테마 개수: " + count);
            }

            // places 테이블 확인
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'places'")) {
                assertTrue(rs.next(), "places 테이블이 존재해야 합니다.");
                System.out.println("[DBUtilTest] places 테이블 생성 확인 완료");
            }

            // quest_templates 테이블 확인
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES LIKE 'quest_templates'")) {
                assertTrue(rs.next(), "quest_templates 테이블이 존재해야 합니다.");
                System.out.println("[DBUtilTest] quest_templates 테이블 생성 확인 완료");
            }

        } catch (Exception e) {
            fail("테이블 검증 쿼리 실행 실패: " + e.getMessage());
        }
    }
}
