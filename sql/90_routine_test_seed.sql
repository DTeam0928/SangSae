-- =====================================================================
-- [테스트·시연용] 루틴 탐색 샘플 데이터 (03, 04 실행 후)
--   동의대학교(부산진구 가야동) 주변 실제 장소 4곳 + 반경 밖 1곳(부산시민공원), 루틴 6개
--   - 좌표는 Google 플러스코드 기준 (POINT(위도 경도))
--   - 사용자 데이터는 넣지 않음 (찜·추가는 로그인해서 직접 테스트)
--   - 장소는 kakao_place_id 'TEST-' 로 시작, 지우려면 맨 아래 정리 쿼리 실행
-- =====================================================================

INSERT INTO places (kakao_place_id, name, road_address, region_1depth_name, region_2depth_name, region_3depth_name, coordinates, category) VALUES
 ('TEST-1', '헬튼 피트니스', '부산 부산진구 가야대로552번길 79 2층', '부산', '부산진구', '가야동', ST_GeomFromText('POINT(35.1501 129.0334)', 4326), 'GYM'),
 ('TEST-2', '멘토즈 스터디카페 부산개금점', '부산 부산진구 개금온정로 6', '부산', '부산진구', '개금동', ST_GeomFromText('POINT(35.1544 129.0252)', 4326), 'CAFE'),
 ('TEST-3', '디저트39 동의대점', '부산 부산진구 가야동 700', '부산', '부산진구', '가야동', ST_GeomFromText('POINT(35.1534 129.0319)', 4326), 'CAFE'),
 ('TEST-4', '동의대 중앙도서관', '부산 부산진구 엄광로 176', '부산', '부산진구', '가야동', ST_GeomFromText('POINT(35.1424 129.0335)', 4326), 'LIBRARY'),
 ('TEST-5', '부산시민공원', '부산 부산진구 시민공원로 73', '부산', '부산진구', '범전동', ST_GeomFromText('POINT(35.1680 129.0590)', 4326), 'PARK');

SET @gym  = (SELECT id FROM places WHERE kakao_place_id = 'TEST-1');
SET @cafe = (SELECT id FROM places WHERE kakao_place_id = 'TEST-2');
SET @dessert = (SELECT id FROM places WHERE kakao_place_id = 'TEST-3');
SET @lib  = (SELECT id FROM places WHERE kakao_place_id = 'TEST-4');
SET @park = (SELECT id FROM places WHERE kakao_place_id = 'TEST-5');

-- 루틴 1: 퇴근 후 헬스 60분
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (@gym, '퇴근 후 헬스 60분 루틴', '퇴근 후 바로 헬스장으로 이동해 워밍업-웨이트-유산소를 연결합니다.', 'EXERCISE', 1840, NOW() - INTERVAL 10 DAY);
SET @r1 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r1, 'EXERCISE'), (@r1, 'WORKER');
INSERT INTO routine_template_tags VALUES (@r1, '웨이트'), (@r1, '퇴근 운동'), (@r1, '초보');
INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category, sort_order) VALUES
 (@r1, NULL, '헬스장까지 10분 걷기', '19:00', '운동', 1), (@r1, @gym, '전신 워밍업 10분', '19:15', '운동', 2),
 (@r1, @gym, '하체 중심 웨이트 25분', '19:30', '운동', 3), (@r1, @gym, '러닝머신 15분', '20:00', '운동', 4),
 (@r1, @gym, '쿨다운 스트레칭', '20:20', '운동', 5);

-- 루틴 2: 헬스장 초보 전신
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (@gym, '헬스장 초보 전신 루틴', '기구를 잘 몰라도 따라가기 쉬운 40분 전신 루틴입니다.', 'EXERCISE', 910, NOW() - INTERVAL 5 DAY);
SET @r2 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r2, 'EXERCISE');
INSERT INTO routine_template_tags VALUES (@r2, '초보'), (@r2, '웨이트');
INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category, sort_order) VALUES
 (@r2, @gym, '워밍업', '18:00', '운동', 1), (@r2, @gym, '머신 3종 순환', '18:20', '운동', 2);

-- 루틴 3: 아침 카페 집중 (스터디카페에서 공부 → 디저트 카페에서 마무리 기록)
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (@cafe, '아침 카페 집중 루틴', '집을 나와 카페에 앉는 행동 자체를 집중 시작 신호로 만드는 루틴입니다.', 'STUDY', 1520, NOW() - INTERVAL 3 DAY);
SET @r3 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r3, 'MIRACLE_MORNING'), (@r3, 'STUDY');
INSERT INTO routine_template_tags VALUES (@r3, '집중'), (@r3, '아침'), (@r3, '독서');
INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category, sort_order) VALUES
 (@r3, @cafe, '카페 도착 · 자리 잡기', '08:00', '공부', 1), (@r3, @cafe, '50분 집중', '08:10', '공부', 2),
 (@r3, @dessert, '디저트 카페에서 오늘 한 일 기록', '09:00', '공부', 3);

-- 루틴 4: 도서관 시험공부
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (@lib, '도서관 시험기간 집중 루틴', '공강 시간에 도서관에서 2시간 몰입하는 루틴입니다.', 'STUDY', 640, NOW() - INTERVAL 2 DAY);
SET @r4 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r4, 'STUDY');
INSERT INTO routine_template_tags VALUES (@r4, '시험기간'), (@r4, '집중'), (@r4, '대학생');
INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category, sort_order) VALUES
 (@r4, @lib, '열람실 자리 잡기', '13:00', '공부', 1), (@r4, @lib, '50분 공부 + 10분 휴식 × 2', '13:10', '공부', 2);

-- 루틴 5: 시민공원 아침 러닝 (반경 3km 밖 → "근처" 섹션에는 안 나와야 함)
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (@park, '시민공원 아침 러닝', '공원 산책로를 따라 5km 러닝', 'HEALTH', 300, NOW() - INTERVAL 1 DAY);
SET @r5 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r5, 'EXERCISE'), (@r5, 'HEALTH');
INSERT INTO routine_template_tags VALUES (@r5, '러닝'), (@r5, '아침');
INSERT INTO routine_template_tasks (routine_template_id, place_id, content, default_time, category, sort_order) VALUES
 (@r5, @park, '러닝 5km', '06:30', '건강', 1);

-- 루틴 6: 장소 없는 루틴 (거리순 정렬에서 맨 뒤)
INSERT INTO routine_templates (representative_place_id, title, description, category, adopted_count, created_at)
VALUES (NULL, '자기 전 스트레칭', '잠들기 전 10분', 'SLEEP', 50, NOW());
SET @r6 = LAST_INSERT_ID();
INSERT INTO routine_template_categories VALUES (@r6, 'SLEEP');
INSERT INTO routine_template_tasks (routine_template_id, content, default_time, category, sort_order) VALUES
 (@r6, '스트레칭 10분', '23:00', '숙면', 1);

-- ---------------------------------------------------------------------
-- 정리 (필요하면 실행): 루틴 → 장소 순서
-- DELETE FROM routine_templates WHERE title IN ('퇴근 후 헬스 60분 루틴','헬스장 초보 전신 루틴','아침 카페 집중 루틴',
--   '도서관 시험기간 집중 루틴','시민공원 아침 러닝','광안리 아침 러닝','자기 전 스트레칭');
-- DELETE FROM places WHERE kakao_place_id LIKE 'TEST-%';
-- ---------------------------------------------------------------------