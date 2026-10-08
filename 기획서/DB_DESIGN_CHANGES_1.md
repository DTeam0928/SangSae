# DB 설계 변경 사항 (R-00 루틴 탐색 · 루틴 관리 탭)

> 기준: `SANGSAE_DB_DESIGN.md` v1.0
> 범위: 회원가입·온보딩·로그인 이후 작업 (루틴 탐색 탭, 루틴 관리 탭)
> 실행 SQL: `sql/03_routine_explore_tables.sql`, `sql/04_routine_explore_tags_place_categories.sql`, `sql/05_routine_manage_once_date.sql`

---

## 0. 요약

| 구분 | 대상 | 내용 | SQL |
|:--|:--|:--|:--|
| 신규 테이블 | `routine_categories` | 루틴 카테고리 칩 마스터 (7종) | 03 |
| 신규 테이블 | `routine_template_categories` | 루틴–카테고리 N:M | 03 |
| 신규 테이블 | `routine_template_bookmarks` | 루틴 찜(♡) | 03 |
| 신규 테이블 | `user_places` | 내가 다니는 장소 | 03 |
| 신규 테이블 | `interest_routine_category_map` | 관심사(O-02) ↔ 루틴 카테고리 연결 (적합도 계산) | 03 |
| 신규 테이블 | `routine_template_tags` | 루틴 자유 태그 | 04 |
| 신규 테이블 | `place_categories` | 장소 유형 마스터 (지도 필터) | 04 |
| 컬럼 추가 | `user_routine_tasks.once_date` | 1회성(ONCE) Task 실행일 | 05 |
| 의미 변경 | `routine_templates.category` | 대표 카테고리 1개 (전체 목록은 `routine_template_categories`) | - |
| 의미 변경 | `routine_templates.creator_id` | NULL = 기본 제공 루틴, 사용자 id = 사용자가 공개한 루틴 | - |
| 의미 변경 | `user_routine_tasks.category`, `routine_template_tasks.category` | `routine_categories.name` 값 저장 (예: `운동`) | - |
| 값 규칙 | `places.category` | `place_categories.code` 와 같은 값 (`GYM`, `CAFE`, `PARK`, `LIBRARY`, `CULTURE`) | - |
| 사용 규칙 | `user_routines.is_active`, `user_routine_tasks.is_active` | 삭제 = 숨김(FALSE). 완료 기록 보존 | - |

기존 테이블의 컬럼 삭제·타입 변경은 없다.

---

## 1. 신규 테이블 명세 (도메인 5 루틴 뒤에 추가)

#### 19. `routine_categories` (루틴 카테고리 마스터)
* **설명**: 루틴 탐색 카테고리 칩, 루틴 카드 태그, Task 카테고리 선택지. 행만 추가·수정하면 화면에 반영
* **화면 연계**: `R-00` 카테고리 칩, 루틴 카드, Task 설정 폼 카테고리

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `code` | VARCHAR(50) | NO | PK | - | 카테고리 코드 (`EXERCISE`, `MIRACLE_MORNING`, `STUDY`, `HEALTH`, `SLEEP`, `WORKER`, `HOBBY`) |
| `name` | VARCHAR(30) | NO | - | - | 표시명 (`운동`, `미라클 모닝`, `공부`, `건강`, `숙면`, `직장인`, `취미`) |
| `emoji` | VARCHAR(10) | YES | - | NULL | 칩 이모지 (💪 🌅 📚 🍎 😴 💼 🎨) |
| `sort_order` | SMALLINT | NO | - | 0 | 칩 노출 순서 |

#### 20. `routine_template_categories` (루틴–카테고리 매핑)
* **설명**: 루틴 하나에 카테고리 여러 개 (N:M 교차 테이블)
* **화면 연계**: `R-00` 카드의 카테고리 태그 여러 개, 칩 필터

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `routine_template_id` | BIGINT | NO | PK, FK | - | `routine_templates.id` 참조 (ON DELETE CASCADE) |
| `category_code` | VARCHAR(50) | NO | PK, FK | - | `routine_categories.code` 참조 (ON UPDATE CASCADE) |

#### 21. `routine_template_tags` (루틴 자유 태그)
* **설명**: 카테고리와 별개인 자유 태그 (예: `웨이트`, `퇴근 운동`, `초보`)
* **화면 연계**: `R-00` 주변 스팟 카드 태그, 검색어 대상, 공개 루틴 설정

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `routine_template_id` | BIGINT | NO | PK, FK | - | `routine_templates.id` 참조 (ON DELETE CASCADE) |
| `tag` | VARCHAR(20) | NO | PK | - | 태그 (`#` 제외, 루틴당 최대 5개) |

#### 22. `routine_template_bookmarks` (루틴 찜)
* **설명**: 루틴 카드의 ♡ (사용자–루틴 교차 테이블)
* **화면 연계**: `R-00` 루틴 카드 찜 버튼

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `user_id` | BIGINT | NO | PK, FK | - | `users.id` 참조 (ON DELETE CASCADE) |
| `routine_template_id` | BIGINT | NO | PK, FK | - | `routine_templates.id` 참조 (ON DELETE CASCADE) |
| `created_at` | DATETIME | NO | - | CURRENT_TIMESTAMP | 찜한 일시 |

#### 23. `user_places` (내가 다니는 장소)
* **설명**: 사용자가 자주 가는 장소 목록 (사용자당 최대 10개, 설정값)
* **화면 연계**: `R-00` "내가 다니는 장소" 셀렉트, 장소 관리, Task 설정 폼 장소 선택지

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `user_id` | BIGINT | NO | PK, FK | - | `users.id` 참조 (ON DELETE CASCADE) |
| `place_id` | BIGINT | NO | PK, FK | - | `places.id` 참조 (ON DELETE CASCADE) |
| `created_at` | DATETIME | NO | - | CURRENT_TIMESTAMP | 등록 일시 (목록 정렬 기준) |

#### 24. `interest_routine_category_map` (관심사–루틴 카테고리 연결)
* **설명**: 온보딩 관심사(O-02)와 루틴 카테고리를 연결해 "관심사 적합도(%)" 계산
* **화면 연계**: `R-00` 당신에게 추천, 카드·장소의 적합도
* **산출**: 적합도 = 루틴 카테고리 중 내 관심사와 연결된 수 / 루틴 카테고리 수

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `interest_category_id` | SMALLINT | NO | PK, FK | - | `interest_categories.id` 참조 (ON DELETE CASCADE) |
| `routine_category_code` | VARCHAR(50) | NO | PK, FK | - | `routine_categories.code` 참조 (ON UPDATE/DELETE CASCADE) |

기본 데이터 (13행)

| 관심사 | 루틴 카테고리 |
|:--|:--|
| `ACTIVITY` #액티비티 | 운동 |
| `WALK` #산책/조깅 | 운동, 건강 |
| `STUDY` #독서/공부 | 공부, 미라클 모닝 |
| `CAFE` #카페 | 공부, 직장인 |
| `NATURE` #힐링/자연 | 건강, 숙면 |
| `FOOD` #맛집 | 건강, 취미 |
| `CULTURE` #문화/예술 | 취미 |
| `PHOTO` #사진 | 취미 |

#### 25. `place_categories` (장소 유형 마스터)
* **설명**: `places.category` 코드의 표시 이름. 행만 추가하면 지도 필터·이름 표시에 반영
* **화면 연계**: `R-00` 지도 [필터] 버튼, 장소 카드 유형 이름

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `code` | VARCHAR(50) | NO | PK | - | 장소 유형 코드 (`GYM`, `CAFE`, `PARK`, `LIBRARY`, `CULTURE`) = `places.category` 값 |
| `name` | VARCHAR(30) | NO | - | - | 표시명 (`헬스장`, `카페`, `공원`, `도서관`, `문화시설`) |
| `emoji` | VARCHAR(10) | YES | - | NULL | 🏋️ ☕ 🌳 📚 🎭 |
| `sort_order` | SMALLINT | NO | - | 0 | 필터 버튼 순서 |

※ `places.category` 에는 FK 를 걸지 않았다 (지도 담당 데이터·카카오 장소 등록과 충돌 방지). 값 규칙으로만 맞춘다.

---

## 2. 기존 테이블 변경

#### 13. `user_routine_tasks` — 컬럼 추가

| 컬럼명(물리) | 데이터 타입 | Nullable | Key | 기본값 | 설명 |
|:--|:--|:--:|:--:|:--:|:--|
| `once_date` | DATE | YES | - | NULL | 1회성(`repeat_type = 'ONCE'`) Task 실행일. 다른 반복 유형은 NULL (`repeat_type` 뒤에 위치) |

설명 문구 수정
- `category`: 예시를 `운동`, `공부`, `건강`, `숙면`, `직장인`, `취미`, `미라클 모닝` (`routine_categories.name`) 으로
- `is_active`: "활성 여부 → FALSE 면 삭제(숨김). 완료 기록(`routine_task_logs`) 보존"

#### 10. `routine_templates` — 설명 수정 (컬럼 변경 없음)

| 컬럼 | 변경 전 | 변경 후 |
|:--|:--|:--|
| `creator_id` | NOT NULL, 개설자 | **NULL 허용**. NULL = 기본 제공 루틴, 사용자 id = 사용자가 루틴 관리 탭에서 공개한 루틴 (현재 DB 도 NULL 허용) |
| `category` | 대표 카테고리 (`EXERCISE`, `GROWTH`, `HEALTH`, `STUDY`) | 대표 카테고리 1개 = `routine_categories.code`. 전체 카테고리는 `routine_template_categories` |
| `visibility` | 공개 설정 | `PUBLIC` 만 탐색 탭에 노출. 사용자가 비공개로 바꾸거나 루틴을 삭제하면 `PRIVATE` |

#### 11. `routine_template_tasks` — 설명 수정
- `category`: `routine_categories.name` 값
- `sort_order`: 사용자가 공개한 루틴은 Task 저장 때마다 `default_time` 순으로 다시 매김

#### 12. `user_routines` — 설명 수정
- `origin_template_id`: 탐색 탭에서 추가한 루틴 → 원본 템플릿 / **직접 만들어 공개한 루틴 → 내가 만든 템플릿(공개용 사본)** / 비공개로 직접 만든 루틴 → NULL
- `is_active`: FALSE = 삭제(숨김)

#### 5. `places` — 설명 수정
- `category`: `place_categories.code` 값 사용 (`GYM`, `CAFE`, `PARK`, `LIBRARY`, `CULTURE`)

---

## 3. 공개 루틴 구조 (설계 원칙 2 "템플릿-Fork" 보충)

사용자가 직접 만든 루틴을 **공개**로 저장하면:

1. `routine_templates` 에 공개용 사본 생성 (`creator_id` = 나, `visibility = 'PUBLIC'`), 카테고리·태그는 19~21번 테이블에 저장
2. 내 `user_routines.origin_template_id` = 그 사본 → 탐색 탭에서 추가해 간 루틴과 같은 구조
3. 내 Task 를 등록·수정·삭제하면 사본 Task(`routine_template_tasks`)도 같이 변경. 1회성 Task 는 사본에 넣지 않음
4. 다른 사용자가 추가해 간 Task 는 영향 없음 (원본 Task 삭제 시 `user_routine_tasks.origin_template_task_id` 만 `ON DELETE SET NULL`)
5. 탐색 탭에서 추가한 루틴(다른 사람·기본 제공 원본)은 공개 불가

---

## 4. 인덱스 추가

| 테이블명 | 인덱스명 | 종류 | 대상 컬럼 | 용도 및 화면 연계 |
|:--|:--|:--|:--|:--|
| `routine_template_categories` | `idx_rtc_category` | Normal | `category_code` | 카테고리 칩 필터 (`R-00`) |
| `routine_template_tags` | `idx_rtt_tag` | Normal | `tag` | 태그 검색 (`R-00`) |
| `routine_template_bookmarks` | `idx_rtb_template` | Normal | `routine_template_id` | 루틴별 찜 조회 (`R-00`) |
| `user_places` | `idx_up_place` | Normal | `place_id` | 장소별 등록 사용자 (`R-00`) |

(각 테이블의 PK 가 사용자·루틴 기준 조회를 담당)

---

## 5. ERD (Mermaid) 추가분

관계 (루틴 관계 블록 아래에 추가)
```mermaid
    routine_templates ||--o{ routine_template_categories : "1:N 카테고리"
    routine_categories ||--o{ routine_template_categories : "분류 매핑"
    routine_templates ||--o{ routine_template_tags : "1:N 자유 태그"
    users ||--o{ routine_template_bookmarks : "찜"
    routine_templates ||--o{ routine_template_bookmarks : "찜 대상"
    users ||--o{ user_places : "내가 다니는 장소"
    places ||--o{ user_places : "등록 장소"
    interest_categories ||--o{ interest_routine_category_map : "관심사 연결"
    routine_categories ||--o{ interest_routine_category_map : "루틴 카테고리 연결"
    place_categories ||..o{ places : "유형 코드 (FK 없음)"
```

엔티티 (엔티티 블록 아래에 추가)
```mermaid
    routine_categories {
        varchar code PK "EXERCISE 등"
        varchar name "운동 등"
        varchar emoji
        smallint sort_order
    }

    routine_template_categories {
        bigint routine_template_id PK "FK routine_templates"
        varchar category_code PK "FK routine_categories"
    }

    routine_template_tags {
        bigint routine_template_id PK "FK routine_templates"
        varchar tag PK "자유 태그"
    }

    routine_template_bookmarks {
        bigint user_id PK "FK users"
        bigint routine_template_id PK "FK routine_templates"
        datetime created_at
    }

    user_places {
        bigint user_id PK "FK users"
        bigint place_id PK "FK places"
        datetime created_at
    }

    interest_routine_category_map {
        smallint interest_category_id PK "FK interest_categories"
        varchar routine_category_code PK "FK routine_categories"
    }

    place_categories {
        varchar code PK "GYM 등 = places.category"
        varchar name "헬스장 등"
        varchar emoji
        smallint sort_order
    }
```

기존 엔티티 수정
```mermaid
    routine_templates {
        bigint creator_id FK "Nullable (NULL=기본 제공)"
        varchar category "대표 routine_categories.code"
    }

    user_routine_tasks {
        date once_date "Nullable, ONCE 실행일"
    }
```

---

## 6. 참고: 다른 담당자 작업으로 설계서와 달라진 부분 (공유 DB 에서 확인)

내 작업은 아니지만 공유 DB 와 설계서가 다르다. ERD 를 고칠 때 해당 담당자에게 확인 후 같이 반영 권장.

| 테이블 | 컬럼 | 공유 DB | 설계서 |
|:--|:--|:--|:--|
| `user_profiles` | `total_xp` | INT NOT NULL DEFAULT 0 (사용자 누적 XP) | 없음 |
| `user_routine_tasks` | `reward_xp` | INT NOT NULL DEFAULT 10 | 없음 (설명·코드에서는 100 XP) |
| `user_routine_tasks` | `allowed_radius_meters` | INT NOT NULL DEFAULT 50 (GPS 인증 반경) | 없음 |
| `routine_template_tasks` | `reward_xp` | INT NOT NULL DEFAULT 10 | 없음 |
| `routine_template_tasks` | `allowed_radius_meters` | 팀원 엔티티에 있음 (DB 추가 여부 확인 필요) | 없음 |
| - | `v_places` (뷰) | 있음 | 없음 |
