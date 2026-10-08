-- =====================================================================
-- 상새(SangSae) 레거시 더미 루틴 정리 및 정식 루틴 활성화 (07_cleanup_dummy_and_refresh_routines.sql)
-- 영문 카테고리(EXERCISE, STUDY 등) 및 비현실적인 더미 수치(1841명 등) 제거
-- =====================================================================

USE sangsae;

-- 1. 과거 테스트용 레거시 더미 루틴 템플릿 제거
-- (ON DELETE CASCADE로 routine_template_tasks, routine_template_categories 자동 정리)
DELETE FROM routine_templates 
WHERE id IN (6, 7, 8, 9, 10) 
   OR title IN ('퇴근 후 헬스 60분 루틴', '헬스장 초보 전신 루틴', '아침 카페 집중 루틴', '도서관 시험기간 집중 루틴', '시민공원 아침 러닝', '자기 전 스트레칭');

-- 2. 실제 부산진구/동의대 생활 밀착형 루틴 템플릿 6종 정식 활성화
-- 카테고리를 한글 표준명으로 일치시키고 자연스러운 실천 인원 수 부여
UPDATE routine_templates SET category = '운동', adopted_count = 482 WHERE id = 11; -- 효민트랙 러닝
UPDATE routine_templates SET category = '건강', adopted_count = 315 WHERE id = 12; -- 가야공원 둘레길
UPDATE routine_templates SET category = '공부', adopted_count = 379 WHERE id = 13; -- 중앙도서관 스터디
UPDATE routine_templates SET category = '운동', adopted_count = 415 WHERE id = 14; -- 개금 헬튼 피트니스
UPDATE routine_templates SET category = '미라클 모닝', adopted_count = 260 WHERE id = 15; -- 시민공원 파워워킹
UPDATE routine_templates SET category = '직장인', adopted_count = 194 WHERE id = 16; -- 전포 카페 독서

-- 3. 확인용 조회 쿼리
SELECT id, title, category, adopted_count 
FROM routine_templates 
WHERE visibility = 'PUBLIC' 
ORDER BY adopted_count DESC;
