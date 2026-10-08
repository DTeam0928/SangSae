-- =====================================================================
-- R-00 루틴 관리 탭: 1회성(ONCE) Task 실행일
--   repeat_type = 'ONCE' 인 Task 는 once_date 하루에만 타임테이블·캘린더에 나온다
--   (다른 반복 유형은 NULL)
-- =====================================================================

ALTER TABLE user_routine_tasks
    ADD COLUMN once_date DATE NULL COMMENT '1회성(ONCE) Task 실행일' AFTER repeat_type;
