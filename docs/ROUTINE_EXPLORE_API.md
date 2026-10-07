# R-00 루틴 탐색 탭 API (프론트 참고)

모든 API 는 로그인 + 온보딩 완료가 필요하다. fetch 에 `X-Requested-With: XMLHttpRequest` 헤더를 붙이면
비로그인 시 로그인 페이지 대신 `401 JSON` 을 받는다.

응답 형식 (인증 API 와 동일)
```json
{"success": true,  "data": { ... }}
{"success": false, "code": "ROUTINE_NOT_FOUND", "field": "global", "message": "루틴을 찾을 수 없습니다."}
```

## 위치

GET API 는 모두 `lat`, `lng` 를 선택으로 받는다 (브라우저 Geolocation 값).
없으면 O-03 에서 저장한 위치 → 그것도 없으면 기본 위치(동의대학교). 응답의 `location.source` 로 구분:
`GPS` / `PROFILE` / `DEFAULT` → DEFAULT 면 `location.label` 로 "기본 위치: 동의대학교 인근" 문구 표시.

---

## 1. 페이지

`GET /routine` → `routine.jsp` (RoutineServlet, 루틴 담당 팀원 코드)
- 카테고리 칩은 `/api/routines/explore` 응답의 `categories` 로 그린다 (`code`, `name`, `emoji`, DB 에서 추가·수정 가능)
- "전체" 칩은 목록에 없음 → 프론트에서 맨 앞에 고정, 값은 `ALL` 또는 빈 값

## 2. 첫 화면 전체 — `GET /api/routines/explore?lat=&lng=&placeCategory=`

`placeCategory`(선택): 지도 [필터]에서 고른 장소 유형 code. 주변 스팟과 "근처에서 뜨는 루틴"에 적용.
필터 버튼 목록은 응답의 `placeCategories` (`code`, `name`, `emoji`) 로 그린다.

| 화면 섹션 | 응답 필드 |
|---|---|
| 지도 핀 + 주변 스팟 카드 3개 | `nearbySpots` (가까운 순, 앞 3개를 카드로) |
| 지금 이 근처에서 뜨는 루틴 | `trendingNearby` (반경 3km, 최근 7일 추가 수 순) |
| 내가 다니는 장소 셀렉트 | `myPlaces` |
| 셀렉트 첫 장소의 인기 루틴 | `myPlaceRoutines` |
| 당신에게 추천 | `recommended` (관심사 적합도 순, 이미 추가한 루틴 제외) |
| 전국 인기 루틴 | `popular` |
| 새로 올라온 루틴 | `latest` |
| 장소별 루틴 아이디어 | **API 없음** — 고정 문구, 프론트에서 처리 |

**루틴 카드** (`RoutineCard`)
```json
{
  "id": 1, "title": "퇴근 후 헬스 60분 루틴", "description": "...",
  "categories": [{"code": "EXERCISE", "name": "운동", "emoji": "💪"}, {"code": "WORKER", "name": "직장인", "emoji": "💼"}],
  "tags": ["웨이트", "초보", "퇴근 운동"],   // 자유 태그
  "place": {"id": 3, "name": "시청 피트니스 짐", "category": "GYM", "categoryName": "헬스장", "roadAddress": "...",
            "latitude": 37.566, "longitude": 126.9775, "distanceMeters": 71},
  "taskCount": 5,
  "adoptedCount": 1840,        // 🔥 1,840명
  "recentAdoptedCount": 12,    // 최근 7일 추가 수
  "fitScore": 100,             // 내 관심사 적합도 %
  "bookmarked": false,         // ♡ 상태
  "createdAt": "2026-10-01T12:00:00"
}
```
`place` 는 장소 없는 루틴이면 `null`.

**주변 스팟** (`NearbySpot`)
```json
{
  "place": { ...장소... },
  "routineCount": 2,
  "tags": ["웨이트", "초보", "퇴근 운동"],      // 그 장소 루틴들의 자유 태그 상위 3개 (없으면 카테고리 이름)
  "timeInsight": "퇴근 후 19~21시 이용 급증",   // 아래 설명
  "fitScore": 96                               // 적합도
}
```

`timeInsight` 규칙
- 실제 Task 완료 기록(최근 7일)이 있으면 가장 많이 몰린 2시간 → **"이용 급증"**(그 전 7일의 1.5배 이상, 3건 이상) 또는 **"이용 많음"**
- 기록이 없으면 Task 예정 시간 기준 → **"루틴 집중"**
- 둘 다 없으면 `null`

**장소 인기 루틴** (`PlaceRoutine`): `{"routineId": 1, "title": "...", "performerCount": 431, "fitScore": 96}`
→ "이 장소 수행자 431명 · 적합도 96%"

## 3. 검색 · 칩 · 정렬 — `GET /api/routines?q=&category=&placeCategory=&sort=&page=&lat=&lng=`

| 파라미터 | 값 |
|---|---|
| `q` | 제목·설명·장소명·카테고리명·자유 태그 검색 (선택) |
| `category` | 칩 code, `ALL`/빈 값이면 전체 |
| `placeCategory` | 대표 장소 유형 code, `ALL`/빈 값이면 전체 |
| `sort` | `distance`(기본, 내 위치 가까운 순) / `popular` / `latest` / `recommend` |
| `page` | 1부터 |

응답: `{"items": [RoutineCard...], "page": 1, "size": 12, "hasNext": true}`

## 4. 루틴 상세 모달 — `GET /api/routines/{id}`

```json
{
  "routine": { ...RoutineCard... },
  "tasks": [{"id": 1, "content": "헬스장까지 10분 걷기", "time": "19:00", "category": "운동", "rewardXp": 10, "placeName": null}],
  "performerCount": 431,     // 지금 실천 중인 인원
  "alreadyAdopted": false    // true 면 [내 루틴에 추가] 비활성
}
```

## 5. 장소 상세 모달 / 내 장소 셀렉트 변경 — `GET /api/routines/places/{placeId}`

`{"spot": NearbySpot, "routines": [PlaceRoutine...]}`

## 6. 찜 (♡)

- `POST /api/routines/{id}/bookmark` → `{"bookmarked": true}`
- `DELETE /api/routines/{id}/bookmark` → `{"bookmarked": false}`

## 7. 내 루틴에 추가 — `POST /user-routines/adopt` (UserRoutineServlet, 루틴 관리 담당 코드)

파라미터 `routineTemplateId` (form 또는 JSON). 성공하면 새 내 루틴(user_routines)이 만들어지고 Task 가 모두 복사되며 🔥 인원 +1.
상세 모달의 `alreadyAdopted` 가 true 면 버튼을 비활성화한다.

## 8. 장소 관리 (내가 다니는 장소) — 응답 data 는 항상 최신 목록

| 요청 | 내용 |
|---|---|
| `GET /api/routines/my-places?lat=&lng=` | 목록 |
| `POST /api/routines/my-places` (form) | 등록 |
| `DELETE /api/routines/my-places/{placeId}` | 삭제 |

POST 파라미터
- 이미 있는 장소: `placeId`
- 카카오 장소 검색 결과: `kakaoPlaceId`(=id), `name`(=place_name), `latitude`(=**y**), `longitude`(=**x**),
  `roadAddress`(=road_address_name), `addressName`(=address_name), `category`(선택)
  > 카카오는 x=경도, y=위도. 반대로 넣으면 엉뚱한 위치에 저장된다.

최대 10개 (`400 PLACE_LIMIT`).

## 에러 코드

| code | 상태 | 상황 |
|---|---|---|
| `ROUTINE_NOT_FOUND` | 404 | 없는 루틴 / 비공개 루틴 |
| `PLACE_NOT_FOUND` | 404 | 없는 장소 |
| `PLACE_INVALID` | 400 | 장소 입력값 오류 |
| `PLACE_LIMIT` | 400 | 장소 등록 개수 초과 |
| `INVALID_INPUT` | 400 | 잘못된 정렬값 / 칩 code / 장소 유형 code |
| `NOT_FOUND` | 404 | 없는 API 경로 |

## 설정 (application.properties, 코드 수정 없이 변경)

`routine.nearby-radius-meters`(3000), `routine.trending-days`(7), `routine.section-size`(6),
`routine.map-place-limit`(20), `routine.page-size`(12), `routine.my-place-limit`(10),
`routine.default-latitude` / `routine.default-longitude` / `routine.default-location-name`(동의대학교),
`routine.usage-days`(7), `routine.surge-ratio`(1.5), `routine.surge-min-count`(3)

## 장소 유형 코드 (place_categories)

`places.category` 와 같은 코드를 쓴다 (지도 담당과 공유): `GYM` 헬스장, `CAFE` 카페, `PARK` 공원, `LIBRARY` 도서관, `CULTURE` 문화시설.
새 유형은 place_categories 에 행만 추가하면 필터와 이름 표시에 반영된다.
