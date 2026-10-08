-- =====================================================================
-- 상새(SangSae) 실제 루틴 스팟 & 템플릿 시드 데이터 (06_routine_spot_seed.sql)
-- 동의대 및 부산진구(가야·개금·전포·시민공원) 실제 위치 기반 검증 데이터
-- =====================================================================

USE sangsae;

-- 1. 루틴 스팟 장소 등록 (실제 주소 및 좌표 기준)
INSERT INTO places (id, kakao_place_id, name, road_address, region_1depth_name, region_2depth_name, region_3depth_name, coordinates, category)
VALUES
 (11, 'SPOT-1', '동의대학교 효민야구장 러닝트랙', '부산 부산진구 엄광로 176', '부산광역시', '부산진구', '가야동', ST_GeomFromText('POINT(35.1438 129.0355)', 4326), 'WALK'),
 (12, 'SPOT-2', '가야공원 숲속 산책로', '부산 부산진구 가야공원로 85', '부산광역시', '부산진구', '가야동', ST_GeomFromText('POINT(35.1472 129.0289)', 4326), 'WALK'),
 (13, 'SPOT-3', '동의대학교 중앙도서관', '부산 부산진구 엄광로 176', '부산광역시', '부산진구', '가야동', ST_GeomFromText('POINT(35.1424 129.0335)', 4326), 'STUDY'),
 (14, 'SPOT-4', '헬튼 피트니스 개금점', '부산 부산진구 가야대로 467 3층', '부산광역시', '부산진구', '개금동', ST_GeomFromText('POINT(35.1528 129.0225)', 4326), 'ACTIVITY'),
 (15, 'SPOT-5', '부산시민공원 하야리아 잔디광장', '부산 부산진구 시민공원로 73', '부산광역시', '부산진구', '범전동', ST_GeomFromText('POINT(35.1680 129.0590)', 4326), 'WALK'),
 (16, 'SPOT-6', '전포 카페거리 골목서가', '부산 부산진구 전포대로209번길 17', '부산광역시', '부산진구', '전포동', ST_GeomFromText('POINT(35.1558 129.0645)', 4326), 'CAFE')
ON DUPLICATE KEY UPDATE 
    name = VALUES(name),
    road_address = VALUES(road_address),
    coordinates = VALUES(coordinates),
    category = VALUES(category);

-- 2. 루틴 템플릿 등록
INSERT INTO routine_templates (id, representative_place_id, title, description, category, visibility, adopted_count)
VALUES
 (11, 11, '효민트랙 아침 공복 3km 러닝', '수업 전 운동장 트랙을 시계 반대방향으로 7바퀴 뛰고 상쾌하게 하루를 시작합니다.', '운동', 'PUBLIC', 342),
 (12, 12, '가야공원 둘레길 식후 25분 피톤치드 산책', '점심 식사 후 가야공원 데크길을 천천히 걸으며 소화시키고 머리를 식힙니다.', '건강', 'PUBLIC', 215),
 (13, 13, '공강 90분 몰입 스터디 루틴', '도서관 2층 노트북 열람실에서 핸드폰을 끄고 과제와 강의 복습에만 집중합니다.', '공부', 'PUBLIC', 489),
 (14, 14, '퇴근길 헬스장 50분 전신 웨이트', '하교 및 퇴근 후 바로 헬스장에 들러 워밍업부터 3대 머신까지 알차게 끝냅니다.', '운동', 'PUBLIC', 620),
 (15, 15, '주말 아침 시민공원 잔디광장 파워워킹', '주말 오전 햇살을 받으며 시민공원 외곽 둘레길 4km를 빠르게 걷습니다.', '미라클 모닝', 'PUBLIC', 178),
 (16, 16, '퇴근 후 카페에서 40분 독서 & 다이어리', '시끄러운 번화가를 벗어나 조용한 전포 골목 카페에서 책 한 권과 하루 회고를 진행합니다.', '직장인', 'PUBLIC', 290)
ON DUPLICATE KEY UPDATE
    representative_place_id = VALUES(representative_place_id),
    title = VALUES(title),
    description = VALUES(description),
    category = VALUES(category),
    adopted_count = VALUES(adopted_count);

-- 3. 루틴 템플릿 세부 Task 등록
INSERT INTO routine_template_tasks (id, routine_template_id, place_id, content, default_time, category, reward_xp, allowed_radius_meters, sort_order)
VALUES
 -- 11: 효민트랙 러닝
 (101, 11, 11, '러닝 전 동적 스트레칭 5분', '07:00:00', '운동', 30, 200, 1),
 (102, 11, 11, '트랙 7바퀴 페이스 러닝 (약 3km)', '07:10:00', '운동', 100, 200, 2),
 (103, 11, 11, '하체 쿨다운 및 수분 보충', '07:35:00', '운동', 20, 200, 3),

 -- 12: 가야공원 산책
 (104, 12, 12, '가야공원 입구에서 데크로드 걷기', '12:40:00', '건강', 50, 300, 1),
 (105, 12, 12, '정자에서 심호흡 3분 휴식', '13:00:00', '건강', 20, 300, 2),

 -- 13: 도서관 스터디
 (106, 13, 13, '열람실 착석 후 핸드폰 비행기모드', '14:00:00', '공부', 20, 150, 1),
 (107, 13, 13, '전공/자격증 뽀모도로 1타임 (50분)', '14:05:00', '공부', 80, 150, 2),
 (108, 13, 13, '학습 요약 5줄 메모', '15:00:00', '공부', 30, 150, 3),

 -- 14: 헬스장 웨이트
 (109, 14, 14, '러닝머신 웜업 10분', '18:30:00', '운동', 30, 150, 1),
 (110, 14, 14, '주요 머신 웨이트 트레이닝 30분', '18:45:00', '운동', 100, 150, 2),
 (111, 14, 14, '폼롤러 스트레칭 10분', '19:15:00', '운동', 20, 150, 3),

 -- 15: 시민공원 파워워킹
 (112, 15, 15, '시민공원 외곽 산책로 4km 걷기', '08:30:00', '운동', 100, 400, 1),
 (113, 15, 15, '잔디광장에서 맨몸 스트레칭', '09:20:00', '건강', 30, 400, 2),

 -- 16: 전포 카페 독서
 (114, 16, 16, '따뜻한 음료 주문 후 책 20페이지 읽기', '19:30:00', '자기계발', 60, 100, 1),
 (115, 16, 16, '오늘 하루 감사한 일 3가지 메모', '20:10:00', '자기계발', 40, 100, 2)
ON DUPLICATE KEY UPDATE
    content = VALUES(content),
    default_time = VALUES(default_time),
    category = VALUES(category),
    reward_xp = VALUES(reward_xp),
    sort_order = VALUES(sort_order);
