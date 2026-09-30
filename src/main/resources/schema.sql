-- ==============================================================
-- 상새 (SangSae) 데이터베이스 초기화 스키마 (v1.0)
-- 일상 루틴 · 장소 기반 퀘스트 · 위치 데이터 · 여행 라이프스타일 플랫폼
-- ==============================================================

CREATE DATABASE IF NOT EXISTS sangsae
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sangsae;

-- 1. 관심사 8대 테마 마스터 (interest_categories)
CREATE TABLE IF NOT EXISTS interest_categories (
    id SMALLINT AUTO_INCREMENT PRIMARY KEY,
    code VARCHAR(30) NOT NULL UNIQUE COMMENT '테마 코드 (FOOD, CAFE, ACTIVITY 등)',
    name VARCHAR(50) NOT NULL COMMENT '표시명 (#맛집, #카페 등)',
    sort_order SMALLINT NOT NULL DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

INSERT INTO interest_categories (code, name, sort_order) VALUES
('FOOD', '#맛집', 1),
('CAFE', '#카페', 2),
('ACTIVITY', '#액티비티', 3),
('PHOTO', '#사진', 4),
('WALK', '#산책/조깅', 5),
('STUDY', '#독서/공부', 6),
('CULTURE', '#문화/예술', 7),
('NATURE', '#힐링/자연', 8)
ON DUPLICATE KEY UPDATE name=VALUES(name), sort_order=VALUES(sort_order);

-- 2. 장소 마스터 (places)
-- 사유: 카카오 로컬 API 장소 식별자(kakao_place_id)로 고속 Get-or-Create 중복 방지 및 SRID 4326 공간 인덱스 적용.
CREATE TABLE IF NOT EXISTS places (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    kakao_place_id VARCHAR(50) UNIQUE COMMENT '카카오 장소 고유 식별 ID',
    name VARCHAR(100) NOT NULL COMMENT '장소명',
    road_address VARCHAR(255) COMMENT '도로명 주소',
    region_1depth_name VARCHAR(50) NOT NULL COMMENT '시/도 (예: 서울특별시)',
    region_2depth_name VARCHAR(50) NOT NULL COMMENT '구/군 (예: 종로구)',
    region_3depth_name VARCHAR(50) NOT NULL COMMENT '동/읍/면 (예: 세종로)',
    coordinates POINT NOT NULL SRID 4326 COMMENT '위경도 공간 좌표 (POINT(위도 경도))',
    category VARCHAR(50) COMMENT '표준 테마 카테고리',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    SPATIAL INDEX sp_places_coordinates (coordinates)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 퀘스트 템플릿 (quest_templates)
CREATE TABLE IF NOT EXISTS quest_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT COMMENT '개설자 users.id',
    place_id BIGINT COMMENT '추천 장소 places.id (GPS 미사용 시 NULL)',
    title VARCHAR(150) NOT NULL COMMENT '퀘스트 제목',
    description TEXT COMMENT '퀘스트 설명',
    category VARCHAR(50) NOT NULL COMMENT '테마 카테고리',
    estimated_minutes INT NOT NULL DEFAULT 30 COMMENT '소요시간(분)',
    reward_xp INT NOT NULL DEFAULT 100 COMMENT '보상 XP',
    auth_type VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '인증 방식 (NONE, PHOTO, LOCATION, TEXT)',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '공개 여부 (PUBLIC, PRIVATE)',
    thumbnail_url VARCHAR(500) COMMENT '썸네일 URL',
    bookmark_count INT NOT NULL DEFAULT 0 COMMENT '북마크 저장 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_quest_tpl_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 사용자 퀘스트 인스턴스 (user_quests - Fork 모델)
CREATE TABLE IF NOT EXISTS user_quests (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '수행 사용자 users.id',
    trip_id BIGINT COMMENT '소속 여행 trips.id (선택)',
    origin_template_id BIGINT COMMENT '원본 quest_templates.id (Fork 계보)',
    place_id BIGINT COMMENT '실천 장소 places.id',
    title VARCHAR(150) NOT NULL COMMENT '퀘스트 제목',
    scheduled_date DATE NOT NULL COMMENT '실천 예정 일자',
    target_time TIME COMMENT '목표 수행 시간',
    auth_type VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '인증 요구 방식',
    status VARCHAR(20) NOT NULL DEFAULT 'TODO' COMMENT '상태 (TODO, COMPLETED, CANCELED)',
    reward_xp INT NOT NULL DEFAULT 100 COMMENT '완료 시 획득 XP',
    memo VARCHAR(255) COMMENT '개인 메모',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at DATETIME COMMENT '최종 완료 일시',
    CONSTRAINT fk_user_quest_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_quest_tpl FOREIGN KEY (origin_template_id) REFERENCES quest_templates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 퀘스트 완료 및 인증 로그 (quest_logs)
CREATE TABLE IF NOT EXISTS quest_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'users.id',
    user_quest_id BIGINT NOT NULL COMMENT 'user_quests.id',
    completed_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '인증 완료 일시',
    auth_type VARCHAR(20) NOT NULL DEFAULT 'NONE' COMMENT '수행된 인증 방식',
    authenticated_location POINT SRID 4326 COMMENT '위치 인증 시 전송된 GPS 좌표',
    proof_image_url VARCHAR(500) COMMENT '사진 인증 이미지 URL',
    proof_text TEXT COMMENT '텍스트 소감/기록',
    earned_xp INT NOT NULL DEFAULT 100 COMMENT '적립된 XP',
    CONSTRAINT fk_quest_log_user_quest FOREIGN KEY (user_quest_id) REFERENCES user_quests(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
