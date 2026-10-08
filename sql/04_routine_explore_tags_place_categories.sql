-- =====================================================================
-- R-00 루틴 탐색 탭 추가분 (03 실행 후 실행)
--   1. routine_template_tags  루틴 자유 태그 (예: 웨이트, 퇴근 운동, 초보) → 주변 스팟 태그
--   2. place_categories       장소 유형 이름표 (places.category 코드 → 화면 이름) → 장소 유형 필터
-- =====================================================================

CREATE TABLE IF NOT EXISTS routine_template_tags (
    routine_template_id BIGINT      NOT NULL,
    tag                 VARCHAR(20) NOT NULL,
    PRIMARY KEY (routine_template_id, tag),
    KEY idx_rtt_tag (tag),
    CONSTRAINT fk_rtt_template FOREIGN KEY (routine_template_id) REFERENCES routine_templates (id) ON DELETE CASCADE
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

CREATE TABLE IF NOT EXISTS place_categories (
    code       VARCHAR(50) NOT NULL,
    name       VARCHAR(30) NOT NULL,
    emoji      VARCHAR(10) NULL,
    sort_order SMALLINT    NOT NULL DEFAULT 0,
    PRIMARY KEY (code)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_unicode_ci;

-- places.category 에 쓰는 코드와 같아야 한다 (지도 담당과 공유)
INSERT INTO place_categories (code, name, emoji, sort_order) VALUES
    ('GYM',     '헬스장',   '🏋️', 1),
    ('CAFE',    '카페',     '☕', 2),
    ('PARK',    '공원',     '🌳', 3),
    ('LIBRARY', '도서관',   '📚', 4),
    ('CULTURE', '문화시설', '🎭', 5)
ON DUPLICATE KEY UPDATE name = VALUES(name), emoji = VALUES(emoji), sort_order = VALUES(sort_order);
