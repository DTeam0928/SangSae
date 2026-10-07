-- =====================================================================
-- R-00 루틴 탐색 탭: 설계서 대비 추가 테이블
--   1. routine_categories            카테고리 칩 마스터 (행만 수정하면 칩이 바뀜)
--   2. routine_template_categories   루틴-카테고리 N:M (카드 태그 여러 개, 칩 필터)
--   3. routine_template_bookmarks    루틴 찜(♡)
--   4. user_places                   내가 다니는 장소
--   5. interest_routine_category_map 관심사(O-02) ↔ 루틴 카테고리 연결 (적합도 계산용)
-- 기존 루틴 테이블과 같은 utf8mb4_unicode_ci 사용
-- =====================================================================

CREATE TABLE IF NOT EXISTS routine_categories (
    code       VARCHAR(50) NOT NULL,
    name       VARCHAR(30) NOT NULL,
    emoji      VARCHAR(10) NULL,
    sort_order SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS routine_template_categories (
    routine_template_id BIGINT      NOT NULL,
    category_code       VARCHAR(50) NOT NULL,
    PRIMARY KEY (routine_template_id, category_code),
    KEY idx_rtc_category (category_code),
    CONSTRAINT fk_rtc_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates (id) ON DELETE CASCADE,
    CONSTRAINT fk_rtc_category FOREIGN KEY (category_code) REFERENCES routine_categories (code) ON UPDATE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS routine_template_bookmarks (
    user_id             BIGINT   NOT NULL,
    routine_template_id BIGINT   NOT NULL,
    created_at          DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, routine_template_id),
    KEY idx_rtb_template (routine_template_id),
    CONSTRAINT fk_rtb_user     FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_rtb_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS user_places (
    user_id    BIGINT   NOT NULL,
    place_id   BIGINT   NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, place_id),
    KEY idx_up_place (place_id),
    CONSTRAINT fk_up_user  FOREIGN KEY (user_id)  REFERENCES users (id)  ON DELETE CASCADE,
    CONSTRAINT fk_up_place FOREIGN KEY (place_id) REFERENCES places (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS interest_routine_category_map (
    interest_category_id  SMALLINT    NOT NULL,
    routine_category_code VARCHAR(50) NOT NULL,
    PRIMARY KEY (interest_category_id, routine_category_code),
    CONSTRAINT fk_ircm_interest FOREIGN KEY (interest_category_id) REFERENCES interest_categories (id) ON DELETE CASCADE,
    CONSTRAINT fk_ircm_category FOREIGN KEY (routine_category_code) REFERENCES routine_categories (code) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- ---------------------------------------------------------------------
-- 기본 데이터
-- ---------------------------------------------------------------------
INSERT INTO routine_categories (code, name, emoji, sort_order) VALUES
    ('EXERCISE',        '운동',       '💪', 1),
    ('MIRACLE_MORNING', '미라클 모닝', '🌅', 2),
    ('STUDY',           '공부',       '📚', 3),
    ('HEALTH',          '건강',       '🍎', 4),
    ('SLEEP',           '숙면',       '😴', 5),
    ('WORKER',          '직장인',     '💼', 6),
    ('HOBBY',           '취미',       '🎨', 7)
ON DUPLICATE KEY UPDATE name = VALUES(name), emoji = VALUES(emoji), sort_order = VALUES(sort_order);

-- 관심사 코드 기준으로 연결 (interest_categories.id 값에 의존하지 않음)
INSERT IGNORE INTO interest_routine_category_map (interest_category_id, routine_category_code)
SELECT ic.id, m.routine_code
FROM interest_categories ic
JOIN (
    SELECT 'ACTIVITY' AS interest_code, 'EXERCISE'        AS routine_code UNION ALL
    SELECT 'WALK',     'EXERCISE'        UNION ALL
    SELECT 'WALK',     'HEALTH'          UNION ALL
    SELECT 'STUDY',    'STUDY'           UNION ALL
    SELECT 'STUDY',    'MIRACLE_MORNING' UNION ALL
    SELECT 'CAFE',     'STUDY'           UNION ALL
    SELECT 'CAFE',     'WORKER'          UNION ALL
    SELECT 'NATURE',   'HEALTH'          UNION ALL
    SELECT 'NATURE',   'SLEEP'           UNION ALL
    SELECT 'FOOD',     'HEALTH'          UNION ALL
    SELECT 'FOOD',     'HOBBY'           UNION ALL
    SELECT 'CULTURE',  'HOBBY'           UNION ALL
    SELECT 'PHOTO',    'HOBBY'
) m ON m.interest_code COLLATE utf8mb4_unicode_ci = ic.code COLLATE utf8mb4_unicode_ci;
