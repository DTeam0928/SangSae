-- ==============================================================
-- 상새 (SangSae) 데이터베이스 초기화 스키마 (v2.0)
-- 일상 루틴 · 장소 기반 퀘스트 · 위치 데이터 · 여행 라이프스타일 플랫폼
-- ==============================================================

CREATE DATABASE IF NOT EXISTS sangsae
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE sangsae;

-- 1. 회원 기본 계정 (users)
-- X(트위터) 로그인은 이메일을 제공하지 않는 경우가 많아 email은 NULL 허용.
-- 동일 소셜 계정 중복 가입 방지를 위해 uk_users_oauth(oauth_provider, oauth_id) 복합 유니크 적용.
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(100) UNIQUE COMMENT '로그인 이메일 (소셜 가입자 NULL 허용)',
    password_hash VARCHAR(255) COMMENT 'BCrypt 암호화 비밀번호 (소셜 가입 시 NULL)',
    oauth_provider VARCHAR(20) NOT NULL DEFAULT 'LOCAL' COMMENT '가입 경로 (LOCAL, GOOGLE, X, APPLE)',
    oauth_id VARCHAR(100) COMMENT '소셜 제공자 고유 식별자',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '계정 상태 (ACTIVE, SUSPENDED, WITHDRAWN)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT uk_users_oauth UNIQUE (oauth_provider, oauth_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 회원 프로필 & 위치 권한 (user_profiles)
-- last_known_location은 온보딩 시 1회만 저장되는 값(실시간 아님). 거부 시 NULL 유지.
CREATE TABLE IF NOT EXISTS user_profiles (
    user_id BIGINT PRIMARY KEY COMMENT 'users.id 참조',
    nickname VARCHAR(30) NOT NULL UNIQUE COMMENT '사용자 닉네임 (1~30자)',
    profile_image_url VARCHAR(500) COMMENT '프로필 이미지 URL',
    is_location_permitted BOOLEAN NOT NULL DEFAULT FALSE COMMENT 'GPS 위치 권한 동의 여부',
    last_known_location POINT SRID 4326 COMMENT '온보딩 시 1회 저장된 최근 GPS 좌표',
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_profiles_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 관심사 8대 테마 마스터 (interest_categories)
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

-- 4. 회원-관심사 매핑 (user_interests)
-- 온보딩(O-02) 건너뛰기 삭제 및 추천 필수화를 위해 1~5개 필수 선택.
CREATE TABLE IF NOT EXISTS user_interests (
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    category_id SMALLINT NOT NULL COMMENT 'interest_categories.id 참조',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, category_id),
    CONSTRAINT fk_user_interests_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_interests_category FOREIGN KEY (category_id) REFERENCES interest_categories(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 장소 마스터 (places)
-- 카카오 로컬 API 장소 식별자(kakao_place_id)로 고속 Get-or-Create 중복 방지 및 SRID 4326 공간 인덱스 적용.
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

-- 6. 사용자 여행 일정 (trips)
CREATE TABLE IF NOT EXISTS trips (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    title VARCHAR(100) NOT NULL COMMENT '여행명 (예: 부산 여행)',
    start_date DATE NOT NULL COMMENT '여행 시작 일자',
    end_date DATE NOT NULL COMMENT '여행 종료 일자',
    status VARCHAR(20) NOT NULL DEFAULT 'PLANNED' COMMENT '상태 (PLANNED, ONGOING, COMPLETED)',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_trips_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_trips_user_date (user_id ASC, start_date ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 퀘스트 템플릿 (quest_templates)
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
    CONSTRAINT fk_quest_tpl_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL,
    INDEX idx_quest_tpl_search (visibility ASC, category ASC, bookmark_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 사용자 퀘스트 인스턴스 (user_quests - Fork 모델)
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
    CONSTRAINT fk_user_quest_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_quest_trip FOREIGN KEY (trip_id) REFERENCES trips(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_quest_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_quest_tpl FOREIGN KEY (origin_template_id) REFERENCES quest_templates(id) ON DELETE SET NULL,
    INDEX idx_user_quests_calendar (user_id ASC, scheduled_date ASC, status ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 9. 사용자 퀘스트 북마크 보관함 (user_quest_bookmarks)
CREATE TABLE IF NOT EXISTS user_quest_bookmarks (
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    quest_template_id BIGINT NOT NULL COMMENT 'quest_templates.id 참조',
    folder_name VARCHAR(50) NOT NULL DEFAULT '기본' COMMENT '보관 폴더명',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, quest_template_id),
    CONSTRAINT fk_uq_bookmarks_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_uq_bookmarks_template FOREIGN KEY (quest_template_id) REFERENCES quest_templates(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 10. 퀘스트 완료 및 인증 로그 (quest_logs)
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
    CONSTRAINT fk_quest_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_quest_log_user_quest FOREIGN KEY (user_quest_id) REFERENCES user_quests(id) ON DELETE CASCADE,
    INDEX idx_quest_logs_user (user_id ASC, completed_at DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 11. 루틴 템플릿 (routine_templates)
CREATE TABLE IF NOT EXISTS routine_templates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT COMMENT '개설자 users.id',
    representative_place_id BIGINT COMMENT '대표 추천 장소 places.id (GPS 미사용 시 NULL)',
    title VARCHAR(100) NOT NULL COMMENT '루틴 패키지명',
    description TEXT COMMENT '루틴 소개 및 효과',
    category VARCHAR(50) NOT NULL COMMENT '대표 카테고리 (EXERCISE, GROWTH, HEALTH, STUDY)',
    visibility VARCHAR(20) NOT NULL DEFAULT 'PUBLIC' COMMENT '공개 설정 (PUBLIC, PRIVATE)',
    adopted_count INT NOT NULL DEFAULT 0 COMMENT '내 루틴에 추가 횟수',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_routine_tpl_place FOREIGN KEY (representative_place_id) REFERENCES places(id) ON DELETE SET NULL,
    INDEX idx_routine_tpl_lookup (visibility ASC, category ASC, adopted_count DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 12. 루틴 템플릿 Task (routine_template_tasks)
CREATE TABLE IF NOT EXISTS routine_template_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    routine_template_id BIGINT NOT NULL COMMENT '소속 루틴 routine_templates.id',
    place_id BIGINT COMMENT 'Task별 추천 장소 places.id',
    content VARCHAR(150) NOT NULL COMMENT '세부 Task 내용',
    default_time TIME NOT NULL DEFAULT '07:00:00' COMMENT '권장 실천 시간',
    category VARCHAR(50) NOT NULL COMMENT '세부 카테고리',
    reward_xp INT NOT NULL DEFAULT 10 COMMENT 'Task 완료 시 획득 XP',
    sort_order SMALLINT NOT NULL DEFAULT 1 COMMENT '패키지 내 순서',
    CONSTRAINT fk_routine_tpl_task_tpl FOREIGN KEY (routine_template_id) REFERENCES routine_templates(id) ON DELETE CASCADE,
    CONSTRAINT fk_routine_tpl_task_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 13. 사용자 활성 루틴 (user_routines - Fork 인스턴스)
CREATE TABLE IF NOT EXISTS user_routines (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '소유 사용자 users.id',
    origin_template_id BIGINT COMMENT '원본 routine_templates.id (Fork 계보)',
    title VARCHAR(100) NOT NULL COMMENT '내 루틴명',
    start_date DATE NOT NULL COMMENT '운영 시작 일자',
    end_date DATE COMMENT '운영 종료 일자 (NULL이면 무기한)',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성화 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_routine_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_routine_origin FOREIGN KEY (origin_template_id) REFERENCES routine_templates(id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 14. 사용자 루틴 Task (user_routine_tasks)
CREATE TABLE IF NOT EXISTS user_routine_tasks (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_routine_id BIGINT NOT NULL COMMENT '소속 루틴 user_routines.id',
    origin_template_task_id BIGINT COMMENT '원본 Task routine_template_tasks.id',
    place_id BIGINT COMMENT '실천 장소 places.id',
    content VARCHAR(150) NOT NULL COMMENT 'Task 내용',
    target_time TIME NOT NULL DEFAULT '07:00:00' COMMENT '타임테이블 정렬 기준 시간',
    category VARCHAR(50) NOT NULL COMMENT '카테고리',
    repeat_type VARCHAR(20) NOT NULL DEFAULT 'DAILY' COMMENT '반복 유형 (DAILY, WEEKDAY, WEEKEND, CUSTOM, ONCE)',
    alert_time TIME COMMENT '푸시 알림 희망 시간',
    reward_xp INT NOT NULL DEFAULT 10 COMMENT 'Task 완료 시 획득할 XP',
    memo VARCHAR(255) COMMENT '개인 메모',
    is_active BOOLEAN NOT NULL DEFAULT TRUE COMMENT '활성 여부',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user_routine_task_routine FOREIGN KEY (user_routine_id) REFERENCES user_routines(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_routine_task_origin FOREIGN KEY (origin_template_task_id) REFERENCES routine_template_tasks(id) ON DELETE SET NULL,
    CONSTRAINT fk_user_routine_task_place FOREIGN KEY (place_id) REFERENCES places(id) ON DELETE SET NULL,
    INDEX idx_tasks_user_time (user_routine_id ASC, target_time ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 15. 루틴 Task 요일 반복 스케줄 (routine_task_schedules)
CREATE TABLE IF NOT EXISTS routine_task_schedules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_routine_task_id BIGINT NOT NULL COMMENT 'user_routine_tasks.id',
    day_of_week VARCHAR(3) NOT NULL COMMENT '반복 요일 (MON, TUE, WED, THU, FRI, SAT, SUN)',
    CONSTRAINT fk_routine_sched_task FOREIGN KEY (user_routine_task_id) REFERENCES user_routine_tasks(id) ON DELETE CASCADE,
    INDEX idx_schedules_day_task (day_of_week ASC, user_routine_task_id ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 16. 루틴 Task 실천 원장 (routine_task_logs)
CREATE TABLE IF NOT EXISTS routine_task_logs (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'users.id',
    user_routine_task_id BIGINT NOT NULL COMMENT 'user_routine_tasks.id',
    log_date DATE NOT NULL COMMENT '실천 일자',
    completed_at TIME NOT NULL COMMENT '완료 처리 시각',
    authenticated_location POINT SRID 4326 COMMENT '인증 시점의 사용자 GPS 좌표',
    earned_xp INT NOT NULL DEFAULT 10 COMMENT '실천 완료 시 적립된 XP',
    is_success BOOLEAN NOT NULL DEFAULT TRUE COMMENT '성공 여부',
    CONSTRAINT fk_routine_log_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_routine_log_task FOREIGN KEY (user_routine_task_id) REFERENCES user_routine_tasks(id) ON DELETE CASCADE,
    INDEX idx_routine_logs_streak (user_id ASC, log_date DESC, is_success ASC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 17. 일일 요약 집계 테이블 (user_daily_aggregates)
CREATE TABLE IF NOT EXISTS user_daily_aggregates (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT 'users.id 참조',
    stat_date DATE NOT NULL COMMENT '집계 기준 일자',
    routine_success_count INT NOT NULL DEFAULT 0 COMMENT '당일 루틴 Task 성공 횟수',
    routine_total_count INT NOT NULL DEFAULT 0 COMMENT '당일 실천 대상 전체 루틴 Task 수',
    quest_success_count INT NOT NULL DEFAULT 0 COMMENT '당일 완료한 퀘스트 수',
    earned_xp INT NOT NULL DEFAULT 0 COMMENT '당일 획득 총 XP',
    current_streak INT NOT NULL DEFAULT 0 COMMENT '당일 기준 연속 달성일수',
    CONSTRAINT fk_daily_agg_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_daily_user_stat (user_id ASC, stat_date DESC)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 18. 홈 대시보드 통계 실시간 계산 뷰 (v_user_home_dashboard)
CREATE OR REPLACE VIEW v_user_home_dashboard AS
SELECT 
    u.id AS user_id,
    COALESCE(trip_stat.ongoing_trip_count, 0) AS ongoing_trip_count,
    COALESCE(routine_stat.active_routine_count, 0) AS active_routine_count,
    COALESCE(quest_stat.completed_quest_count, 0) AS completed_quest_count,
    COALESCE(quest_stat.remaining_quest_count, 0) AS remaining_quest_count,
    COALESCE(today_routine_stat.today_done_tasks, 0) AS today_done_tasks,
    GREATEST(0, COALESCE(today_routine_stat.today_total_tasks, 0) - COALESCE(today_routine_stat.today_done_tasks, 0)) AS today_remaining_tasks,
    COALESCE(total_xp_stat.total_earned_xp, 0) AS total_earned_xp
FROM users u
LEFT JOIN (
    SELECT user_id, COUNT(*) AS ongoing_trip_count
    FROM trips
    WHERE status = 'ONGOING'
    GROUP BY user_id
) trip_stat ON u.id = trip_stat.user_id
LEFT JOIN (
    SELECT user_id, COUNT(*) AS active_routine_count
    FROM user_routines
    WHERE is_active = TRUE
    GROUP BY user_id
) routine_stat ON u.id = routine_stat.user_id
LEFT JOIN (
    SELECT 
        user_id,
        COUNT(CASE WHEN status = 'COMPLETED' THEN 1 END) AS completed_quest_count,
        COUNT(CASE WHEN status = 'TODO' THEN 1 END) AS remaining_quest_count
    FROM user_quests
    GROUP BY user_id
) quest_stat ON u.id = quest_stat.user_id
LEFT JOIN (
    SELECT 
        urt.user_routine_id,
        ur.user_id,
        COUNT(DISTINCT urt.id) AS today_total_tasks,
        COUNT(DISTINCT rtl.id) AS today_done_tasks
    FROM user_routines ur
    JOIN user_routine_tasks urt ON ur.id = urt.user_routine_id AND urt.is_active = TRUE
    LEFT JOIN routine_task_logs rtl ON urt.id = rtl.user_routine_task_id 
        AND rtl.log_date = CURDATE() AND rtl.is_success = TRUE
    WHERE ur.is_active = TRUE
    GROUP BY ur.user_id
) today_routine_stat ON u.id = today_routine_stat.user_id
LEFT JOIN (
    SELECT 
        user_id,
        SUM(earned_xp) AS total_earned_xp
    FROM (
        SELECT user_id, earned_xp FROM quest_logs
        UNION ALL
        SELECT user_id, earned_xp FROM routine_task_logs WHERE is_success = TRUE
    ) logs
    GROUP BY user_id
) total_xp_stat ON u.id = total_xp_stat.user_id;
