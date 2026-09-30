package com.dteam0928.sangsae.common.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

public final class DBUtil {

    private static volatile HikariDataSource dataSource;

    private DBUtil() {}

    public static DataSource getDataSource() {
        if (dataSource == null) {
            synchronized (DBUtil.class) {
                if (dataSource == null) {
                    dataSource = initDataSource();
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    private static HikariDataSource initDataSource() {
        HikariConfig config = new HikariConfig();

        String directUrl = ConfigUtil.get("DB_URL");
        if (directUrl != null && !directUrl.isBlank()) {
            config.setJdbcUrl(directUrl.trim());
        } else {
            String host = ConfigUtil.get("DB_HOST", "cdn.ditanet.duckdns.org");
            String port = ConfigUtil.get("DB_PORT", "8306");
            String dbName = ConfigUtil.get("DB_NAME", "sangsae");

            // 사유: 원격 MySQL 환경에서의 SSL 핸드셰이크 오류 방지 및 한글 깨짐/타임존 불일치를 방지하기 위한 필수 쿼리 파라미터 구성.
            String jdbcUrl = String.format(
                "jdbc:mysql://%s:%s/%s?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
                host, port, dbName
            );
            config.setJdbcUrl(jdbcUrl);
        }

        String username = ConfigUtil.get("DB_USER");
        if (username == null || username.isBlank()) {
            username = ConfigUtil.get("DB_USERNAME");
        }
        if (username == null || username.isBlank()) {
            username = ConfigUtil.get("MYSQL_USER", "root");
        }

        String password = ConfigUtil.get("DB_PASSWORD");
        if (password == null) {
            password = ConfigUtil.get("DB_PASS");
        }
        if (password == null) {
            password = ConfigUtil.get("MYSQL_PASSWORD", "");
        }

        config.setUsername(username.trim());
        config.setPassword(password.trim());
        config.setDriverClassName("com.mysql.cj.jdbc.Driver");

        // 사유: 원격 DB 네트워크 핑 지연을 고려해 커넥션 획득 타임아웃을 10초로 여유 있게 부여하고 풀 크기 제어.
        config.setPoolName("SangSae-HikariPool");
        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(10000);
        config.setIdleTimeout(300000);
        config.setMaxLifetime(1800000);

        return new HikariDataSource(config);
    }

    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            System.err.println("[SangSae DBUtil] DB 커넥션 테스트 실패: " + e.getMessage());
            return false;
        }
    }

    // 사유: 원격 DB에 데이터베이스(CREATE DATABASE) 및 테이블이 아직 없을 때 최초 1회 초기화를 원클릭으로 수행.
    public static synchronized boolean initDatabaseAndSchema() {
        String host = ConfigUtil.get("DB_HOST", "cdn.ditanet.duckdns.org");
        String port = ConfigUtil.get("DB_PORT", "8306");
        String dbName = ConfigUtil.get("DB_NAME", "sangsae");

        String username = ConfigUtil.get("DB_USER", ConfigUtil.get("DB_USERNAME", "root"));
        String password = ConfigUtil.get("DB_PASSWORD", ConfigUtil.get("DB_PASS", ""));

        String serverUrl = String.format(
            "jdbc:mysql://%s:%s/?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=UTF-8",
            host, port
        );

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("[SangSae DBUtil] MySQL 드라이버를 찾을 수 없습니다: " + e.getMessage());
            return false;
        }

        try (java.sql.Connection conn = java.sql.DriverManager.getConnection(serverUrl, username.trim(), password.trim());
             java.sql.Statement stmt = conn.createStatement()) {

            stmt.executeUpdate("CREATE DATABASE IF NOT EXISTS " + dbName + " DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci");
            System.out.println("[SangSae DBUtil] 데이터베이스 '" + dbName + "' 생성/확인 완료");

            stmt.executeUpdate("USE " + dbName);

            try (java.io.InputStream is = DBUtil.class.getClassLoader().getResourceAsStream("schema.sql")) {
                if (is != null) {
                    String sql = new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
                    
                    // 사유: SQL 파일 내 한 줄 주석(--)이 각 실행 구문 선두에 위치하여 전체 CREATE TABLE 구문이 건너뛰어지는 현상 방지.
                    StringBuilder cleanSql = new StringBuilder();
                    for (String line : sql.split("\\r?\\n")) {
                        String trimmedLine = line.trim();
                        if (!trimmedLine.startsWith("--")) {
                            cleanSql.append(line).append("\n");
                        }
                    }

                    String[] statements = cleanSql.toString().split(";");
                    for (String stmtSql : statements) {
                        String trimmed = stmtSql.trim();
                        if (!trimmed.isEmpty()) {
                            stmt.execute(trimmed);
                        }
                    }
                    System.out.println("[SangSae DBUtil] schema.sql 전체 테이블 초기화 완료");
                }
            }

            return true;
        } catch (Exception e) {
            System.err.println("[SangSae DBUtil] DB/테이블 초기화 실패: " + e.getMessage());
            return false;
        }
    }

    public static synchronized void closeDataSource() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            dataSource = null;
        }
    }
}
