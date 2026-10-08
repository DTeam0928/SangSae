# R-00 루틴 관리 탭 API (프론트 참고)

탐색 탭 API와 같은 규칙: 로그인 필요, fetch 에 `X-Requested-With: XMLHttpRequest` 헤더, 응답은
`{"success": true, "data": ...}` / `{"success": false, "code": "...", "field": "title", "message": "..."}`.
`field` 가 있으면 그 입력칸 아래에 `message` 를 표시한다.

등록·수정은 **form(POST)** 으로 보낸다. 여러 값(카테고리, 태그, 요일)은 같은 이름을 반복한다.
```js
const body = new URLSearchParams();
body.append('title', '아침 루틴');
['EXERCISE', 'WORKER'].forEach(c => body.append('categories', c));
fetch('/sangsae/api/my-routines', {method: 'POST', body, headers: {'X-Requested-With': 'XMLHttpRequest'}});
```
날짜는 `yyyy-MM-dd`, 시간은 `HH:mm`.

---

## 화면 ↔ API

| 화면 | API |
|---|---|
| 탭 첫 진입 (셀렉트, 카테고리 선택지, 장소 선택지) | `GET /api/my-routines` |
| 요약 카드 3개 + 루틴 캘린더 | `GET /api/my-routines/{id}/calendar?from=&to=` |
| 오늘의 루틴 타임테이블 | `GET /api/my-routines/{id}/timeline?date=` |
| + 새 루틴 | `POST /api/my-routines` |
| ⚙ 기본 정보 (열기 / 저장 / 루틴 삭제) | `GET` / `POST` / `DELETE /api/my-routines/{id}` |
| Task 설정 [저장] (새 Task / 수정) | `POST /api/my-routines/{id}/tasks` / `POST /api/my-routines/{id}/tasks/{taskId}` |
| Task 설정 [삭제] | `DELETE /api/my-routines/{id}/tasks/{taskId}` |
| 완료 체크 ○ | **팀원 API** `POST /user-routines/tasks/complete` (GPS 인증·XP 지급, 체크 해제 불가) |

---

## 1. 첫 진입 — `GET /api/my-routines`

```json
{
  "routines": [MyRoutine...],     // 만든 순. 비어 있으면 "새 루틴 만들기" 안내 화면
  "categories": [{"code": "EXERCISE", "name": "운동", "emoji": "💪"}, ...],   // Task 카테고리·공개 카테고리 선택지
  "myPlaces": [장소...]           // Task 장소 선택지 (내가 다니는 장소)
}
```

**MyRoutine**
```json
{
  "id": 3, "title": "아침 루틴",
  "startDate": "2026-10-05", "endDate": "2026-12-31",   // endDate 없으면 null → "종료일 없음"
  "visibility": "PUBLIC",          // PUBLIC = 탐색 탭에 공개 중
  "adopted": false,                // true = 탐색 탭에서 추가한 루틴 → 공개 설정 비활성화
  "description": "...", "categories": [...], "tags": ["아침"], "representativePlace": {장소} | null,
  "taskCount": 4
}
```
운영 기간 표시: `운영 기간 {startDate} ~ {endDate}`

## 2. 캘린더 + 요약 카드 — `GET /api/my-routines/{id}/calendar?from=&to=`

- 주 보기: `from` = 그 주 일요일, `to` = 토요일 (생략하면 이번 주)
- 월 보기: 달력에 보이는 첫 칸 ~ 마지막 칸 (최대 42일)

```json
{
  "routineId": 3, "from": "2026-10-04", "to": "2026-10-10",
  "days": [{"date": "2026-10-05", "planned": 3, "done": 1}, ...],   // planned = 점 개수, done = 완료 수
  "achievementRate": 27,   // 선택 기간 달성률 % (오늘까지만 계산, 미래 예정 제외)
  "streakDays": 3,         // 연속 실천일 (하루 1개 이상 완료, 오늘 기록이 없으면 어제까지)
  "totalXp": 1240          // 누적 XP (사용자 전체)
}
```

## 3. 타임테이블 — `GET /api/my-routines/{id}/timeline?date=`

`date` 생략하면 오늘. 운영 기간 밖이면 `tasks` 가 빈 목록.
```json
{
  "routineId": 3, "date": "2026-10-08", "doneCount": 1,
  "tasks": [{
    "id": 5, "content": "헬스장 가기", "time": "07:00",
    "categoryCode": "EXERCISE", "categoryName": "운동", "categoryEmoji": "💪",
    "repeatType": "DAILY", "repeatLabel": "매일 반복",      // 화면 "💪 운동 · 매일 반복"
    "daysOfWeek": [], "onceDate": null,
    "alertTime": "06:50", "memo": null, "place": {장소} | null, "allowedRadiusMeters": 50,
    "done": true, "completedAt": "07:03"                    // done 이면 ○ 대신 완료 표시 + 취소선
  }]
}
```
Task 를 클릭하면 이 값으로 Task 설정 폼을 채운다.

## 4. 새 루틴 / 기본 정보 저장

`POST /api/my-routines` (새 루틴), `POST /api/my-routines/{id}` (수정) → 응답: MyRoutine

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `title` | O | 100자 이내 |
| `startDate` | | 없으면 오늘 |
| `endDate` | | 없으면 종료일 없음. 시작일 이후 |
| `visibility` | | `PUBLIC` / `PRIVATE` (기본) |
| `categories` | 공개면 O | 1~3개, 반복 |
| `tags` | | 5개, 각 20자 이내, 반복 (`#` 은 빼고 저장) |
| `description` | | 500자 이내 |
| `representativePlaceId` | | 탐색 탭 카드에 보일 대표 장소 |

- 공개로 저장하면 탐색 탭에 바로 나온다. 그 뒤 Task 를 바꾸면 탐색 탭 내용도 같이 바뀐다
  (이미 추가해 간 다른 사용자의 루틴은 그대로).
- 1회성 Task 는 탐색 탭에 공개되지 않는다.
- `adopted: true` 루틴을 공개로 저장하면 `400 ROUTINE_VISIBILITY_LOCKED`.

## 5. 루틴 삭제 — `DELETE /api/my-routines/{id}`

숨김 처리(완료 기록·XP 보관). 공개 중이었다면 탐색 탭에서도 내려간다. 응답: 1번과 같은 첫 진입 데이터.

## 6. Task 등록·수정·삭제

- 등록 `POST /api/my-routines/{id}/tasks?date=`
- 수정 `POST /api/my-routines/{id}/tasks/{taskId}?date=`
- 삭제 `DELETE /api/my-routines/{id}/tasks/{taskId}?date=` (숨김, 완료 기록 보관)

응답: `date`(지금 보고 있는 날짜, 생략하면 오늘)의 타임테이블 → 받은 값으로 목록을 다시 그린다.
캘린더 점 개수도 바뀌므로 저장 후 2번을 한 번 더 부른다.

| 파라미터 | 필수 | 설명 |
|---|---|---|
| `content` | O | 루틴 내용, 150자 이내 |
| `time` | O | `HH:mm` |
| `category` | O | 카테고리 code |
| `placeId` | | 장소 id. 카카오 검색 결과는 먼저 `POST /api/routines/my-places` 로 등록해서 id 를 받는다 |
| `repeatType` | | `DAILY`(매일, 기본) / `WEEKDAY`(평일) / `WEEKEND`(주말) / `CUSTOM`(사용자 지정) / `ONCE`(1회성) |
| `daysOfWeek` | CUSTOM 이면 O | `MON`~`SUN`, 반복 |
| `onceDate` | ONCE 면 O | 운영 기간 안의 날짜 |
| `alertTime` | | `HH:mm` |
| `memo` | | 255자 이내 |
| `allowedRadiusMeters` | | 장소 Task 의 GPS 인증 허용 반경(m). 50~1000, 없으면 50 (범위 밖이면 가장 가까운 값으로 저장) |

## 에러 코드

| code | field | 상황 |
|---|---|---|
| `ROUTINE_NOT_FOUND` (404) | | 없는·삭제한·남의 루틴 |
| `TASK_NOT_FOUND` (404) | | 없는·삭제한 Task |
| `ROUTINE_TITLE_INVALID` | title | 이름 없음 / 100자 초과 |
| `ROUTINE_PERIOD_INVALID` | endDate | 날짜 형식 / 종료일 < 시작일 |
| `ROUTINE_CATEGORY_INVALID` | categories | 공개인데 카테고리 0개 / 4개 이상 / 없는 code |
| `ROUTINE_TAG_INVALID` | tags | 6개 이상 / 21자 이상 |
| `ROUTINE_DESCRIPTION_INVALID` | description | 500자 초과 |
| `ROUTINE_VISIBILITY_LOCKED` | visibility | 탐색 탭에서 추가한 루틴을 공개 |
| `TASK_CONTENT_INVALID` | content | 내용 없음 / 150자 초과 |
| `TASK_TIME_INVALID` | time | 시간 없음 / 형식 오류 |
| `TASK_CATEGORY_INVALID` | category | 카테고리 없음 / 없는 code |
| `TASK_REPEAT_INVALID` | repeatType | 반복값 오류 / 요일 없음 / 1회성 날짜 없음·기간 밖 |
| `TASK_ALERT_INVALID` | alertTime | 알림 시간 형식 오류 |
| `TASK_MEMO_INVALID` | memo | 255자 초과 |
| `PLACE_NOT_FOUND` (404) | place | 없는 장소 id |
| `INVALID_INPUT` | | 날짜 형식 오류, 캘린더 기간 42일 초과 등 |
