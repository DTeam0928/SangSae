# 마이페이지 API (프론트 참고) — 닉네임·관심사·위치

다른 API 와 같은 규칙: 로그인 필요, fetch 에 `X-Requested-With: XMLHttpRequest` 헤더, 응답은
`{"success": true, "message": "...", "data": ...}` / `{"success": false, "code": "...", "field": "nickname", "message": "..."}`.
`field` 가 있으면 그 입력칸 아래에 `message` 를 표시한다. 저장은 form(POST) 방식.

| 화면 (카드) | API |
|---|---|
| 화면 진입 | `GET /mypage` → `src/main/webapp/WEB-INF/views/mypage/mypage.jsp` (JSP 는 프론트가 만든다) |
| 첫 데이터 (닉네임, 관심사 칩, 위치 허용 여부) | `GET /api/me/profile` |
| 닉네임 변경 [변경하기] | `POST /api/me/nickname` |
| 관심사 변경 [저장하기] | `POST /api/me/interests` |
| 위치 권한 허용 [권한 허용하기] | `POST /api/me/location` |
| 테마 설정 | 백엔드 없음 (브라우저 localStorage 등 프론트에서 처리) |
| 로그아웃 | `POST /auth/logout` (기존. GET 링크로는 안 됨 → `<form method="post">` 버튼) |

모든 저장 API 의 응답 `data` 는 아래 1번과 같은 최신 값 → 받은 값으로 화면을 다시 그리면 된다.

---

## 1. 조회 — `GET /api/me/profile`

```json
{
  "email": "godlife@questrip.demo",
  "provider": "LOCAL",              // LOCAL / GOOGLE / X / APPLE
  "providerName": "이메일",          // 화면 표시용
  "nickname": "작심삼일졸업반",
  "nicknameMaxLength": 30,
  "interests": [                     // 전체 관심사 8개 (표시 순서대로)
    {"id": 1, "code": "FOOD", "name": "#맛집", "selected": false},
    {"id": 2, "code": "CAFE", "name": "#카페", "selected": true}
  ],
  "selectedInterestIds": [2, 3, 4, 6],
  "maxInterestCount": 5,
  "locationPermitted": true          // true 면 위치 카드에 "허용됨" 표시 (버튼은 "현재 위치로 다시 저장" 등)
}
```
`name` 에 `#` 이 붙어 있으니 칩에 그대로 쓰거나 `#` 을 빼고 쓴다.

## 2. 닉네임 변경 — `POST /api/me/nickname`

| 파라미터 | 설명 |
|---|---|
| `nickname` | 앞뒤 공백 제거 후 1~30자, 다른 사람과 중복 불가 (내 현재 닉네임 그대로면 통과) |

관심사는 건드리지 않는다. 저장하면 로그인 세션 닉네임도 바뀌어서 다른 화면도 다음에 열 때부터 새 닉네임으로 나온다.

## 3. 관심사 변경 — `POST /api/me/interests`

| 파라미터 | 설명 |
|---|---|
| `interestIds` | 관심사 id, 0~5개, 같은 이름 반복. 하나도 안 보내면 전부 해제 |

```js
const body = new URLSearchParams();
document.querySelectorAll('.chip.selected').forEach(c => body.append('interestIds', c.dataset.id));
const res = await fetch('/sangsae/api/me/interests', {
  method: 'POST', body, headers: {'X-Requested-With': 'XMLHttpRequest'}
}).then(r => r.json());
alert(res.message);   // 성공: "관심사를 저장했습니다." / 실패: field=interests 의 에러 문구
```
닉네임은 건드리지 않는다. 바꾼 관심사는 루틴 탐색 탭의 적합도·추천에 바로 반영된다.

## 4. 위치 권한 허용 — `POST /api/me/location`

| 파라미터 | 설명 |
|---|---|
| `latitude`, `longitude` | 브라우저 `navigator.geolocation.getCurrentPosition` 으로 받은 좌표 |

```js
navigator.geolocation.getCurrentPosition(pos => {
  const body = new URLSearchParams({latitude: pos.coords.latitude, longitude: pos.coords.longitude});
  fetch('/sangsae/api/me/location', {method: 'POST', body, headers: {'X-Requested-With': 'XMLHttpRequest'}})
    .then(r => r.json()).then(res => alert(res.message));
}, () => alert('브라우저에서 위치 권한을 허용해 주세요.'));   // 거부하면 API 를 부르지 않는다
```
저장한 좌표는 루틴 탐색 탭에서 GPS 를 쓰지 않을 때의 추천 기준 위치가 된다.

## 에러 코드

| code (HTTP) | field | 상황 |
|---|---|---|
| `NICKNAME_REQUIRED` (400) | nickname | 닉네임 없음 / 공백만 |
| `NICKNAME_LENGTH` (400) | nickname | 30자 초과 |
| `NICKNAME_DUPLICATED` (409) | nickname | 다른 사람이 쓰는 닉네임 |
| `INTEREST_LIMIT` (400) | interests | 6개 이상 |
| `INTEREST_INVALID` (400) | interests | 없는 id / 숫자가 아닌 값 |
| `LOCATION_INVALID` (400) | location | 좌표 없음 / 범위 밖 |
| `UNAUTHORIZED` (401) | | 로그인 풀림 → 로그인 화면으로 |

참고: `POST /api/me/profile` (nickname + interestIds 한 번에) 도 남아 있다. 한 버튼으로 둘 다 저장하는 화면이면 이걸 쓴다.
