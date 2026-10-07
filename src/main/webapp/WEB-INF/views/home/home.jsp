<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="/WEB-INF/views/common/head.jsp" %>
    <title>상새 - Home</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/home.css">
    <script src="${pageContext.request.contextPath}/assets/js/api-client.js" defer></script>
    <script src="${pageContext.request.contextPath}/assets/js/home.js" defer></script>
</head>
<body data-page="home" data-context-path="${pageContext.request.contextPath}">

<%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="container" id="home-dashboard">
    <div class="section-title">
        <span class="title">
            <span id="user-nickname">게스트</span>님의 종합 대시보드
        </span>
    </div>

    <div class="dashboard-grid">
        <!-- 0. XP 및 오늘의 달성도 카드 (고정 레이아웃) -->
        <section class="dash-card xp-card">
            <div class="dash-card-header">
                <div class="dash-title">🎯 오늘의 달성도</div>
                <span class="badge xp-badge" id="user-streak-badge">🔥 연속 0일</span>
            </div>

            <div class="xp-content-group">
                <div class="xp-info-row">
                    <span class="stat-label">오늘 획득한 XP</span>
                    <div class="stat-num stat-num-accent">
                        <span id="earned-xp-num">0</span>
                        <span class="xp-unit">XP</span>
                    </div>
                </div>

                <div class="xp-progress-bar-container" role="progressbar" aria-label="오늘의 진행도">
                    <div class="xp-progress-bar" id="xp-progress" style="width: 0%;"></div>
                </div>

                <div class="xp-next-target">
                    <span>루틴 완료 현황</span>
                    <span class="text-accent-bold" id="routine-status-text">0 / 0개</span>
                </div>
            </div>
        </section>

        <!-- 1. 내 여행 관리 -->
        <div class="dash-card">
            <div>
                <div class="dash-card-header">
                    <div class="dash-title">✈️ 내 여행 관리</div>
                    <select class="badge select-badge" id="trip-filter">
                        <option value="active">진행 중 여행 ▾</option>
                        <option value="planned">예정된 여행</option>
                        <option value="completed">완료된 여행</option>
                    </select>
                </div>

                <div id="trip-content" class="list-container">
                    <div class="empty-state">등록된 여행이 없습니다.<br>첫 여행을 만들어보세요!</div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/quest#management" class="detail-btn">여행 및 퀘스트 추가하기</a>
        </div>

        <!-- 2. 내 루틴 관리 -->
        <div class="dash-card">
            <div>
                <div class="dash-card-header">
                    <div class="dash-title">🔄 내 루틴 관리</div>
                    <select class="badge select-badge orange" id="routine-filter">
                        <option value="active">활성 루틴 ▾</option>
                        <option value="inactive">비활성 루틴</option>
                    </select>
                </div>

                <div id="routine-content" class="list-container">
                    <div class="empty-state">활성 루틴이 없습니다.<br>새 루틴을 추가해보세요!</div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/routine#manage" class="detail-btn">루틴 추가하기</a>
        </div>

        <!-- 3. 통계 카드 (퀘스트 & 루틴 달성도) -->
        <div class="dash-card">
            <div class="dash-title">🔥 내 달성 현황</div>
            <div class="stats-row">
                <div>
                    <div class="stat-label">달성한 퀘스트</div>
                    <div class="stat-num" id="stat-completed-quests">0개</div>
                </div>
                <div>
                    <div class="stat-label">남은 퀘스트</div>
                    <div class="stat-num stat-num-accent" id="stat-remaining-quests">0개</div>
                </div>
                <div style="border-left: 1px solid var(--color-border); padding-left: 12px;">
                    <div class="stat-label">달성한 할 일</div>
                    <div class="stat-num" id="stat-completed-routines">0개</div>
                </div>
            </div>
        </div>
    </div>

    <!-- 추가 섹션: 오늘의 추천 퀘스트 및 커뮤니티 하이라이트 -->
    <div class="extra-section">
        <div class="section-title"><span>🌟 오늘의 추천 퀘스트 & 소식</span></div>
        <div class="dashboard-grid">
            <div class="full-card card-flex-col">
                <div>
                    <div class="card-header-flex">
                        <span class="badge">추천 챌린지</span>
                        <span class="text-accent-bold" id="rec-quest-xp">+120 XP</span>
                    </div>
                    <div class="subtitle card-title-gap" id="rec-quest-title">광안리에서 일몰 사진 남기기</div>
                    <p class="desc" id="rec-quest-desc">해 질 무렵 바다를 바라보며 마음에 드는 사진 한 장을 남겨보세요.</p>
                </div>
                <a href="${pageContext.request.contextPath}/quest" class="detail-btn">퀘스트 탐색하러 가기</a>
            </div>

            <div class="full-card card-flex-col">
                <div>
                    <div class="card-header-flex">
                        <span class="badge orange">커뮤니티 피드</span>
                        <span class="desc" id="community-time">10분 전</span>
                    </div>
                    <div class="subtitle card-title-gap" id="community-title">광안대교 야경 보면서 산책하기 완료! ✨</div>
                    <p class="desc" id="community-desc">"밤바다가 너무 예쁘네요. 다들 오늘 산책 어떠신가요?" - 여행러버</p>
                </div>
                <a href="${pageContext.request.contextPath}/community" class="detail-btn btn-community">커뮤니티 구경하기</a>
            </div>
        </div>
    </div>
</main>

<%@ include file="/WEB-INF/views/common/modal_delete.jsp" %>
</body>
</html>