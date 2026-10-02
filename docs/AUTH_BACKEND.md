# 인증 · 온보딩 백엔드 (O-00 ~ O-03)

담당: 이메일 로그인/회원가입, Google 로그인, 프로필·관심사(O-02), 위치 권한(O-03)
기준: Java 21 · Servlet 6.0 · JSP/JSTL 3.0 · Hibernate 6.6 (JPA) · MySQL · Tomcat 10.1 · Gradle

---

## 1. 로컬 실행

1. **DB**: 팀 공용 DB(`sangsae`) 사용. 테이블은 `sql/01_auth_tables.sql` → `sql/02_seed_interest_categories.sql` 로 이미 생성됨
2. **개인 설정**: `src/main/resources/application-local.properties.example` 을 같은 폴더에
   `application-local.properties` 로 복사하고 값 입력 (git 에 올라가지 않음)
   ```properties
   db.username=...
   db.password=...
   oauth.google.client-id=...        # 개인 채널로 공유받기
   oauth.google.client-secret=...
   oauth.state-cookie.secure=false   # 로컬(http) 테스트용
   # Tomcat 포트가 8080 이 아니면 (예: 80)
   # app.base-url=http://localhost/sangsae
   ```
3. `gradlew clean war` → war 파일을 Tomcat `webapps` 에 **`sangsae.war`** 로 복사 → `http://localhost:8080/sangsae/`
   (war 이름이 컨텍스트 경로가 됨. Google 리디렉션 URI 와 맞아야 함)

DB 접속 정보는 `persistence.xml` 에 적지 않는다. `AppContext` 가 설정 파일로 커넥션 풀(HikariCP)을 만들어
Hibernate 에 넘긴다. **앱 시작 시 DB 연결에 실패하면 앱 전체가 404** → Tomcat `logs/localhost.날짜.log` 확인.

`build.gradle` 의 의존성은 팀 통합본으로 맞춰져 있음. 라이브러리 추가가 필요하면 직접 고치지 말고 공유 후 반영.

---

## 2. 팀 공용으로 쓸 수 있는 부분

| 클래스 | 용도 |
|---|---|
| `AppContext` | 객체 조립소. 자기 DAO/Service 를 "각 기능 Service" 영역에 추가하고 getter 추가 |
| `common.db.TransactionTemplate` | `app().tx().inTransaction(em -> {...})` 트랜잭션 + 예외 시 자동 롤백 |
| `web.page.BaseServlet` | `app()`, `loginUser(req)`, `forward()`, `redirect()`, `forwardWithError()`, `param()` |
| `auth.web.LoginSession` | `LoginSession.current(req)` → 로그인 사용자(`LoginUser`: id, email, nickname, provider) |
| `common.exception.ErrorCode` / `BusinessException` | 사용자 안내 문구 모음 + 검증 실패 예외 |
| `common.web.ApiResponse`, `WebUtil.writeJson` | AJAX 공통 JSON 형식 |
| `common.geo.GeoPoint` | 위경도. 저장 `GeoPoint.sqlFromWkt(n)` + `toWkt()` (아래 좌표 규칙 참고) |
| `web.filter.AuthFilter` | 모든 페이지 로그인 필수. 비로그인 공개 페이지는 `AccessRules` 에 경로 추가 |

```java
// 다른 기능 서블릿 예시
@WebServlet("/quests")
public class QuestServlet extends BaseServlet {
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ... {
        long userId = loginUser(req).getId();
        req.setAttribute("quests", app().questService().findMyQuests(userId));
        forward(req, resp, "/WEB-INF/views/quest/list.jsp");
    }
}
```

`AppContext` 의 DAO/Service 는 모든 요청이 공유하는 객체이므로 **요청별 값(로그인 사용자, 입력값)을 필드에 저장하지 말 것**.

---

## 3. 패키지 구조 (`com.dteam0928.sangsae`)

```
AppContext                 객체 조립 (DataSource → EntityManagerFactory → DAO → Service)
common/                    팀 공용: config, db, exception, validation, geo, web
user/model                 @Entity: User, UserProfile, InterestCategory, UserInterest
user/dao (인터페이스) └ jpa/  JPA 구현
user/service               UserAccountService(가입 공통), 기본 닉네임 생성
auth/                      dto, model(LoginUser), service(이메일·소셜), oauth(공통 구조 + Google), web(서블릿)
onboarding/                O-02, O-03
web/                       AuthFilter, AppContextListener, BaseServlet, HomeServlet(임시)
```

요청 흐름: Servlet → Service(`tx.inTransaction`) → DAO(`EntityManager` 로 JPQL/native)

---

## 4. URL 계약 (프론트 참고)

| 화면 | 요청 | 파라미터 | 결과 |
|---|---|---|---|
| O-00 | GET `/` | – | `auth/landing.jsp`. 로그인 상태면 `/home` |
| O-00 | GET `/oauth/google` | – | Google 로그인 페이지 |
| – | GET `/oauth/google/callback` | Google 이 전송 | 기존 회원 `/home`, 신규 `/onboarding/profile`, 실패·취소·중복 이메일 `/` + `${flashMessage}` |
| O-01 | GET `/auth/login`, `/auth/signup` | – | 탭 = 페이지 |
| O-01 | POST `/api/auth/email-check` (AJAX) | `email` | `ApiResponse` JSON |
| O-01 | POST `/auth/login` | `email, password` | `/home` |
| O-01 | POST `/auth/signup` | `email, password, passwordConfirm` | 자동 로그인 → `/onboarding/profile` |
| O-02 | POST `/onboarding/profile` | `nickname, interestIds`(여러 개) | `/onboarding/location` |
| O-03 | POST `/onboarding/location` | `action=allow\|deny\|later, latitude, longitude` | `/home` |
| – | POST `/auth/logout` | – | `/` (GET 불가) |

- 랜딩 JSP: Google 버튼 링크 `${pageContext.request.contextPath}/oauth/google`,
  활성화 여부 `${enabledProviders.contains('google')}`, 안내 문구 `${flashMessage}` (한 번만 표시)
- 검증 실패 시 같은 JSP 를 다시 보여주며 `${errors.email}`, `${errors.password}`, `${errors.passwordConfirm}`,
  `${errors.nickname}`, `${errors.interests}`, `${errors.global}` 로 인라인 에러를 꺼낸다.

---

## 5. 정책

| 항목 | 구현 |
|---|---|
| 이메일 | trim + 소문자 저장. 중복확인 통과 이메일을 세션에 저장해 가입 시 재확인, 동시 가입은 UNIQUE 로 최종 방어 |
| 비밀번호 | 영문+숫자 포함 8~64자, 공백 불가, 특수문자 허용. BCrypt |
| 1 이메일 = 1 가입 수단 | 다른 수단으로 이미 가입된 이메일이면 가입 거부 + "이미 가입된 이메일입니다" 안내 |
| 기본 닉네임 | 가입 즉시 `여행자4821` 형식 자동 생성 (`nickname.prefixes`, `nickname.digits` 설정으로 변경) |
| O-02 | 건너뛰기 없음. 닉네임 필수(자동 생성값 미리 채움, 수정 가능), 관심사 최소 1개 |
| 온보딩 | 가입 직후 세션에만 "온보딩 전" 표시 → 필터가 O-02 로 보냄. 기존 회원 로그인은 바로 H-00 (DB 컬럼 추가 없음) |
| 세션 | 로그인 시 세션 ID 재발급, SameSite=Lax, 시간 제한 없음(로그아웃 또는 브라우저 종료까지) |

### DB UNIQUE 제약

| 테이블 | 제약 | 의미 |
|---|---|---|
| users | `uk_users_email (email)` | 같은 이메일 1개. NULL 은 여러 개 허용 |
| users | `uk_users_oauth (oauth_provider, oauth_id)` | **설계서 대비 추가.** 같은 소셜 계정 중복 가입 방지. LOCAL 은 oauth_id NULL 이라 영향 없음 |
| user_profiles | `uk_profiles_nickname (nickname)` | 닉네임 중복 방지 |
| user_interests | PK `(user_id, category_id)` | 같은 관심사 중복 선택 방지 |

### 좌표 규칙 (모든 POINT 컬럼 공통)

- SRID 4326, 순서는 **`POINT(위도 경도)`**
- 저장: `ST_GeomFromText('POINT(35.1532 129.1187)', 4326)` / 조회: `ST_Latitude(col)`, `ST_Longitude(col)`
- **카카오 API 는 x=경도, y=위도** → `POINT(y x)` 로 넣어야 함 (순서가 바뀌어도 에러 없이 저장되므로 주의)
- 거리: `ST_Distance_Sphere(a, b)` (미터)
- (0, 0) 및 범위 밖 좌표는 거부
- `user_profiles.last_known_location` 은 NULL 가능(위치 거부/나중에), O-03 에서 한 번만 저장되는 값이라 실시간 위치 아님

---

## 6. 소셜 로그인 설정

현재 **Google 만 구현**. X, Apple 은 이후 추가 예정.
client-id 가 비어 있는 제공자는 "준비 중" 안내만 나가고 앱은 정상 동작한다.

**Google**: Google Cloud Console → Google Auth Platform → 클라이언트(웹 애플리케이션)
- 승인된 리디렉션 URI: `{app.base-url}/oauth/google/callback`
  (등록됨: `http://localhost:8080/sangsae/oauth/google/callback`, `http://localhost/sangsae/oauth/google/callback`)
- 앱이 **테스트 모드**라 테스트 사용자로 등록된 계정만 로그인 가능. 시연 전 계정 추가 또는 앱 게시 필요
- 서버 배포 시: 해당 도메인 리디렉션 URI 추가, `app.base-url` 변경, `oauth.state-cookie.secure=true`

새 제공자(예: X, Apple, 카카오): `AuthProvider` 에 추가 → `AbstractOAuthProvider` 상속 클래스 작성 →
`OAuthProviderRegistry.fromConfig` 에 등록. 서블릿·서비스·DB 는 수정 없음.
