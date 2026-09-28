# 상새 (SangSae)
> **일상을 새롭게** — 일상 루틴 · 장소 기반 퀘스트 · 여행 라이프스타일 플랫폼

---

## 📌 프로젝트 소개
- **프로젝트명:** 상새 (SangSae)
- **슬로건:** 일상을 새롭게
- **도메인:** 일상 루틴 관리, 위치/장소 기반 퀘스트, 여행 일정 연계 플랫폼

---

## 🛠 기술 스택
- **Language:** Java 21
- **Web Engine:** Jakarta EE Servlet 6.0, JSP (Model 2 MVC)
- **ORM / Persistence:** Hibernate 6.6.x (Jakarta Persistence 3.0)
- **Build Tool:** Gradle 8.14
- **Web Application Server:** Apache Tomcat 10.1+

---

## 📂 프로젝트 구조
```text
src/
├── main/
│   ├── java/
│   │   └── com/dteam0928/sangsae/   # 백엔드 서블릿 및 비즈니스 로직
│   ├── resources/
│   │   └── META-INF/persistence.xml # JPA/Hibernate 설정
│   └── webapp/
│       ├── WEB-INF/web.xml          # 웹 배포 서술자
│       └── index.jsp                # 메인 뷰
└── 기획서/
    ├── 화면설계서/
    └── SANGSAE_DB_DESIGN.md         # 데이터베이스 설계서 (v1.0)
```
