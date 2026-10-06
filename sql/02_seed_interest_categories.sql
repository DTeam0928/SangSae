-- O-02 관심사 테마 칩 8종 (DB 설계서 interest_categories 코드/표시명 그대로)
INSERT INTO interest_categories (code, name, sort_order) VALUES
    ('FOOD',     '#맛집',      1),
    ('CAFE',     '#카페',      2),
    ('ACTIVITY', '#액티비티',  3),
    ('PHOTO',    '#사진',      4),
    ('WALK',     '#산책/조깅', 5),
    ('STUDY',    '#독서/공부', 6),
    ('CULTURE',  '#문화/예술', 7),
    ('NATURE',   '#힐링/자연', 8)
ON DUPLICATE KEY UPDATE name = VALUES(name), sort_order = VALUES(sort_order);
