-- ==============================================================
-- 상새 (SangSae) 실제 테스트용 시드 데이터 (seed.sql)
-- HeidiSQL 또는 MySQL 콘솔에서 실행하여 실제 데이터를 주입할 수 있습니다.
-- ==============================================================

USE sangsae;

-- 1. 실제 장소 데이터 등록
INSERT INTO places (id, kakao_place_id, name, road_address, region_1depth_name, region_2depth_name, region_3depth_name, coordinates, category)
VALUES 
(1, 'K101', '광안리 해수욕장', '부산 수영구 광안해변로 219', '부산광역시', '수영구', '광안동', ST_GeomFromText('POINT(35.1532 129.1186)', 4326), '관광지'),
(2, 'K102', '전포 카페거리', '부산 부산진구 전포대로 209', '부산광역시', '부산진구', '전포동', ST_GeomFromText('POINT(35.1555 129.0664)', 4326), '카페'),
(3, 'K103', '해운대 동백섬 산책로', '부산 해운대구 동백로 67', '부산광역시', '해운대구', '우동', ST_GeomFromText('POINT(35.1541 129.1524)', 4326), '산책로')
ON DUPLICATE KEY UPDATE name=VALUES(name);

-- 2. 실제 퀘스트 템플릿 등록
INSERT INTO quest_templates (id, place_id, title, description, category, estimated_minutes, reward_xp, auth_type, visibility)
VALUES 
(1, 1, '[DB] 광안리 선셋 일몰 보기', '해 질 무렵 광안리에서 바다를 바라보며 하루를 마무리하세요.', '사진', 40, 500, 'LOCATION', 'PUBLIC'),
(2, 2, '[DB] 전포 로컬 카페 방문하기', '전포동 카페거리의 조용한 카페에서 커피 한 잔의 여유를 즐기세요.', '카페', 60, 500, 'LOCATION', 'PUBLIC'),
(3, 3, '[DB] 해운대 동백섬 30분 산책', '파도 소리를 들으며 동백섬 둘레길을 가볍게 걸어보세요.', '산책', 30, 500, 'LOCATION', 'PUBLIC')
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 3. 실제 루틴 템플릿 등록
INSERT INTO routine_templates (id, representative_place_id, title, description, category, visibility, adopted_count)
VALUES 
(1, 1, '[DB] 아침 바다 조깅 30분', '해변 도로를 따라 가볍게 러닝하고 상쾌하게 하루를 시작합니다.', '운동', 'PUBLIC', 120),
(2, 2, '[DB] 카페 모닝 독서 루틴', '조용한 카페에서 40분간 책을 읽고 하루 계획을 세웁니다.', '성장', 'PUBLIC', 85)
ON DUPLICATE KEY UPDATE title=VALUES(title);

-- 4. 루틴 템플릿 세부 Task 등록
INSERT INTO routine_template_tasks (id, routine_template_id, place_id, content, default_time, category, reward_xp, allowed_radius_meters)
VALUES 
(1, 1, 1, '광안리 해변 3km 러닝', '07:00:00', '운동', 100, 200),
(2, 1, 1, '쿨다운 스트레칭', '07:30:00', '운동', 50, 200),
(3, 2, 2, '모닝 독서 40분', '08:00:00', '자기계발', 100, 100)
ON DUPLICATE KEY UPDATE content=VALUES(content);
