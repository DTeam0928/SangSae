package com.dteam0928.sangsae.common.db;

import com.dteam0928.sangsae.common.config.AppConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * DB 커넥션 풀(HikariCP) 생성.
 * DB 접속 정보는 persistence.xml 이 아니라 application(-local).properties 에서 읽는다.
 * → 계정/비밀번호가 git 에 올라가지 않고, 팀원마다 다른 로컬 DB 를 쓸 수 있다.
 */
public final class DataSourceFactory {

    private DataSourceFactory() {
    }

    public static HikariDataSource create(AppConfig config) {
        HikariConfig hc = new HikariConfig();
        hc.setDriverClassName(config.get("db.driver", "com.mysql.cj.jdbc.Driver"));
        hc.setJdbcUrl(config.require("db.url"));
        hc.setUsername(config.require("db.username"));
        hc.setPassword(config.get("db.password", ""));
        hc.setMaximumPoolSize(config.getInt("db.pool.max-size", 10));
        hc.setMinimumIdle(config.getInt("db.pool.min-idle", 2));
        hc.setPoolName("sangsae-pool");
        return new HikariDataSource(hc);
    }
}
