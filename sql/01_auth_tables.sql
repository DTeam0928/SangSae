-- =====================================================================
-- 상새(SangSae) 회원·온보딩 테이블 DDL — DB 설계서 v1.0 [도메인 1] 기준 (MySQL 8.0)
--   users, user_profiles, interest_categories, user_interests
--
-- 설계서 대비 차이점 (팀 합의 필요, ★ 표시)
--   ★1. users.email NULL 허용      : X(Twitter) 로그인은 이메일을 주지 않는 경우가 많아
--                                     NOT NULL 이면 X 가입이 불가능함
--   ★2. users (oauth_provider, oauth_id) UNIQUE 추가 (uk_users_oauth)
--                                   : 같은 소셜 계정이 동시에 두 번 가입되는 것 방지
-- 좌표 규칙은 설계서대로 POINT(위도 경도), SRID 4326
-- =====================================================================

CREATE TABLE IF NOT EXISTS users (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    email          VARCHAR(100) NULL,                                   -- ★1 설계서: NOT NULL
    password_hash  VARCHAR(255) NULL,
    oauth_provider VARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
    oauth_id       VARCHAR(100) NULL,
    status         VARCHAR(20)  NOT NULL DEFAULT 'ACTIVE',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    UNIQUE KEY uk_users_email (email),
    UNIQUE KEY uk_users_oauth (oauth_provider, oauth_id)                -- ★2 추가
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS user_profiles (
    user_id               BIGINT       NOT NULL,
    nickname              VARCHAR(30)  NOT NULL,
    profile_image_url     VARCHAR(500) NULL,
    is_location_permitted BOOLEAN      NOT NULL DEFAULT FALSE,
    last_known_location   POINT SRID 4326 NULL,
    updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id),
    UNIQUE KEY uk_profiles_nickname (nickname),
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS interest_categories (
    id         SMALLINT    NOT NULL AUTO_INCREMENT,
    code       VARCHAR(30) NOT NULL,
    name       VARCHAR(50) NOT NULL,
    sort_order SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    UNIQUE KEY uk_interest_categories_code (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;

CREATE TABLE IF NOT EXISTS user_interests (
    user_id     BIGINT   NOT NULL,
    category_id SMALLINT NOT NULL,
    created_at  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id),
    CONSTRAINT fk_user_interests_user     FOREIGN KEY (user_id)     REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_interests_category FOREIGN KEY (category_id) REFERENCES interest_categories (id)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4;
