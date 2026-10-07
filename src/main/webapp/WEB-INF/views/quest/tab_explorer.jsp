<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 검색 및 필터 바 -->
<div class="searchbar">
    <div class="search-wrapper">
        <input id="searchInput" class="form-input" placeholder="🔍 퀘스트, 장소, 테마를 검색해보세요" oninput="App.quest.handleSearch(this.value)">
        <button class="clear-search desc" id="clearSearchBtn" onclick="App.quest.clearSearch()">✕</button>
    </div>
    <button class="select-btn subtitle" id="regionFilterBtn" onclick="App.quest.openRegionModal()">📍 부산 전체 ▾</button>
</div>

<!-- 검색 결과 영역 -->
<div id="searchResults" class="search-results">
    <div class="section-head">
        <div class="title" id="searchResultTitle">'검색어' 결과</div>
    </div>
    <div class="layout-grid" id="searchGrid"></div>
</div>

<!-- 탐색 메인 컨텐츠 -->
<div id="exploreHome" class="explore-home">

    <!-- 히어로 배너 -->
    <div class="hero">
        <div>
            <div class="badges"><span class="badge">QUEST DISCOVERY</span></div>
            <h1 class="quest-detail-5 title">오늘 어떤 경험을 해볼까요?</h1>
            <p class="desc">여행지에서 할 수 있는 작은 도전을 발견하고 나만의 일정으로 만들어보세요.</p>
        </div>
        <div class="hero-actions">
            <button class="btn btn-secondary" onclick="App.quest.openCollectionModal()">♡ 내 컬렉션</button>
            <button class="quest-detail-6 btn btn-primary" onclick="App.quest.openCreateModal()">＋ 퀘스트 만들기</button>
        </div>
    </div>

    <!-- 카테고리 칩 -->
    <div class="quick">
        <button class="chip subtitle active" onclick="App.quest.setCategory(this, '전체')">✨ 전체</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '맛집')">🍴 맛집</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '카페')">☕ 카페</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '사진')">📸 사진</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '산책')">🚶 산책</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '문화')">🎨 문화</button>
        <button class="chip subtitle" onclick="App.quest.setCategory(this, '액티비티')">🎢 액티비티</button>
    </div>

    <!-- 오늘의 추천 -->
    <div class="section">
        <div class="section-head">
            <div>
                <div class="title">🌟 오늘의 추천</div>
                <div class="desc">현재 시간과 여행지에 맞춰 골라봤어요</div>
            </div>
        </div>
        <div class="feature">
            <div class="feature-main">
                <div class="badges">
                    <span class="badge">오늘 추천</span>
                    <span class="badge orange">+120 XP</span>
                </div>
                <h2 class="quest-detail-5 title">광안리에서 일몰 사진 남기기</h2>
                <p class="quest-detail-7 desc">해 질 무렵 바다를 바라보며 마음에 드는 사진 한 장을 남겨보세요.</p>
                <div class="desc">📍 수영구 · 광안리 · ⏱ 30~40분 · ⭐ 4.8</div>
                <div class="quest-detail-8">
                    <button class="quest-detail-6 btn btn-primary" onclick="App.quest.openDetailModal(1)">퀘스트 보기</button>
                    <button class="btn btn-secondary" onclick="App.quest.toggleSave(1, this)">♡</button>
                </div>
            </div>
            <div class="feature-side">
                <div class="quest-detail-9 dash-card">
                    <strong class="subtitle">🌅 추천 시간</strong>
                    <span class="desc">오늘 18:20 ~ 19:10</span>
                </div>
                <div class="quest-detail-9 dash-card">
                    <strong class="subtitle">🎯 난이도</strong>
                    <span class="desc">★★☆☆☆ 쉬움</span>
                </div>
                <div class="quest-detail-9 dash-card">
                    <strong class="subtitle">🏆 완료 보상</strong>
                    <span class="quest-detail-10 desc">+120 XP · 일몰 기록 배지</span>
                </div>
            </div>
        </div>
    </div>

    <!-- 내 주변 퀘스트 (지도) -->
    <div class="section">
        <div class="section-head">
            <div>
                <div class="title">📍 내 주변 퀘스트</div>
                <div class="desc">지도에서 장소와 퀘스트를 함께 확인하세요</div>
            </div>
            <div class="quest-detail-11 subtitle" onclick="App.quest.showToast('지도 기능을 준비 중입니다.')">지도 크게 보기 →</div>
        </div>
        <div class="map-layout">
            <div class="map">
                <div class="quest-detail-12 subtitle">
                    부산 · 주변 12개
                </div>
            </div>
            <div class="map-list" id="mapList"></div>
        </div>
    </div>

    <!-- 코스 / 인기 / 신규 섹션 -->
    <div class="section">
        <div class="section-head">
            <div>
                <div class="title">🗺️ 함께 담는 여행 코스</div>
                <div class="desc">여러 퀘스트를 한 번에 일정에 추가할 수 있어요</div>
            </div>
        </div>
        <div class="course-grid" id="courseGrid"></div>
    </div>

    <div class="section">
        <div class="section-head">
            <div>
                <div class="title">🔥 지금 인기</div>
                <div class="desc">다른 여행자들이 많이 담은 퀘스트</div>
            </div>
        </div>
        <div class="layout-grid" id="popularGrid"></div>
    </div>

    <div class="section">
        <div class="section-head">
            <div class="title">🆕 새로 올라온 퀘스트</div>
        </div>
        <div class="layout-grid" id="newGrid"></div>
    </div>
</div>
