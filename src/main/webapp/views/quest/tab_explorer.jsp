<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<style>
    /* 탐색 탭 전용 스타일 */
    .hero {
        background: var(--color-sub-bg);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-lg);
        padding: 28px;
        display: flex;
        justify-content: space-between;
        gap: 24px;
        align-items: center;
        margin-bottom: 20px;
    }
    .hero-actions { display: flex; gap: 9px; flex-wrap: wrap; }

    .searchbar { display: flex; gap: 8px; margin-bottom: 18px; position: relative; }
    .search-wrapper { flex: 1; position: relative; display: flex; align-items: center; }
    .clear-search {
        position: absolute;
        right: 12px;
        background: var(--color-sub-bg);
        border: 0;
        width: 24px;
        height: 24px;
        border-radius: 50%;
        display: none;
        align-items: center;
        justify-content: center;
        cursor: pointer;
    }
    .select-btn {
        height: 46px;
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
        background: var(--color-white);
        padding: 0 16px;
        cursor: pointer;
    }

    .quick { display: flex; gap: 9px; overflow-x: auto; padding-bottom: 5px; margin-bottom: 22px; }
    .chip {
        white-space: nowrap;
        border: 1px solid var(--color-border);
        background: var(--color-white);
        border-radius: 999px;
        padding: 8px 16px;
        cursor: pointer;
    }
    .chip.active, .chip:hover {
        border-color: var(--color-dark);
        background: var(--color-main);
        color: var(--color-dark);
    }

    .search-results { display: none; }
    .search-results.active { display: block; }
    .explore-home.hidden { display: none; }

    .section { margin: 28px 0; }
    .section-head { display: flex; justify-content: space-between; align-items: flex-end; margin-bottom: 13px; }

    .feature {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-lg);
        padding: 20px;
        display: grid;
        grid-template-columns: 1.25fr 0.75fr;
        gap: 20px;
    }
    .feature-main {
        min-height: 220px;
        border-radius: var(--radius-md);
        background: var(--color-main);
        padding: 24px;
        display: flex;
        flex-direction: column;
        justify-content: flex-end;
    }
    .feature-side { display: flex; flex-direction: column; gap: 10px; justify-content: space-between; }

    .grid, .course-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 14px; }
    .card { cursor: pointer; transition: transform .15s; }
    .card:hover { transform: translateY(-3px); }
    .thumb {
        height: 120px;
        border-radius: var(--radius-md);
        background: var(--color-sub-bg);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 36px;
        margin-bottom: 12px;
    }

    .card-bottom { display: flex; justify-content: space-between; align-items: center; margin-top: auto; padding-top: 12px; }
    .card-actions { display: flex; gap: 6px; }

    .map-layout { display: grid; grid-template-columns: 1.2fr .8fr; gap: 15px; }
    .map {
        height: 380px;
        border-radius: var(--radius-lg);
        background: var(--color-sub-bg);
        position: relative;
        border: 1px solid var(--color-border);
    }
    .map-list {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-lg);
        padding: 16px;
        max-height: 380px;
        overflow-y: auto;
    }
    .map-item { padding: 12px 0; border-bottom: 1px solid var(--color-border2); cursor: pointer; }
    .map-item:last-child { border-bottom: 0; }

    @media(max-width: 850px) {
        .feature, .map-layout { grid-template-columns: 1fr; }
        .grid, .course-grid { grid-template-columns: repeat(2, 1fr); }
    }
    @media(max-width: 600px) {
        .grid, .course-grid { grid-template-columns: 1fr; }
    }
</style>

<!-- 검색 및 필터 바 -->
<div class="searchbar">
    <div class="search-wrapper">
        <input id="searchInput" class="form-input" placeholder="🔍 퀘스트, 장소, 테마를 검색해보세요" oninput="handleSearch(this.value)">
        <button class="clear-search desc" id="clearSearchBtn" onclick="clearSearch()">✕</button>
    </div>
    <button class="select-btn subtitle" id="regionFilterBtn" onclick="openRegionModal()">📍 부산 전체 ▾</button>
</div>

<!-- 검색 결과 영역 -->
<div id="searchResults" class="search-results">
    <div class="section-head">
        <div class="title" id="searchResultTitle">'검색어' 결과</div>
    </div>
    <div class="grid" id="searchGrid"></div>
</div>

<!-- 탐색 메인 컨텐츠 -->
<div id="exploreHome" class="explore-home">

    <!-- 히어로 배너 -->
    <div class="hero">
        <div>
            <div class="badges"><span class="badge">QUEST DISCOVERY</span></div>
            <h1 class="title" style="margin-bottom: 8px;">오늘 어떤 경험을 해볼까요?</h1>
            <p class="desc">여행지에서 할 수 있는 작은 도전을 발견하고 나만의 일정으로 만들어보세요.</p>
        </div>
        <div class="hero-actions">
            <button class="btn btn-secondary" onclick="openCollectionModal()">♡ 내 컬렉션</button>
            <button class="btn btn-primary" style="margin-top:0;" onclick="openCreateModal()">＋ 퀘스트 만들기</button>
        </div>
    </div>

    <!-- 카테고리 칩 -->
    <div class="quick">
        <button class="chip subtitle active" onclick="setCategory(this, '전체')">✨ 전체</button>
        <button class="chip subtitle" onclick="setCategory(this, '맛집')">🍴 맛집</button>
        <button class="chip subtitle" onclick="setCategory(this, '카페')">☕ 카페</button>
        <button class="chip subtitle" onclick="setCategory(this, '사진')">📸 사진</button>
        <button class="chip subtitle" onclick="setCategory(this, '산책')">🚶 산책</button>
        <button class="chip subtitle" onclick="setCategory(this, '문화')">🎨 문화</button>
        <button class="chip subtitle" onclick="setCategory(this, '액티비티')">🎢 액티비티</button>
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
                <h2 class="title" style="margin-bottom: 8px;">광안리에서 일몰 사진 남기기</h2>
                <p class="desc" style="margin-bottom: 14px;">해 질 무렵 바다를 바라보며 마음에 드는 사진 한 장을 남겨보세요.</p>
                <div class="desc">📍 수영구 · 광안리 · ⏱ 30~40분 · ⭐ 4.8</div>
                <div style="margin-top:15px; display:flex; gap:8px;">
                    <button class="btn btn-primary" style="margin-top:0;" onclick="openDetailModal(1)">퀘스트 보기</button>
                    <button class="btn btn-secondary" onclick="toggleSave(1, this)">♡</button>
                </div>
            </div>
            <div class="feature-side">
                <div class="dash-card" style="min-height: auto; padding: 14px;">
                    <strong class="subtitle">🌅 추천 시간</strong>
                    <span class="desc">오늘 18:20 ~ 19:10</span>
                </div>
                <div class="dash-card" style="min-height: auto; padding: 14px;">
                    <strong class="subtitle">🎯 난이도</strong>
                    <span class="desc">★★☆☆☆ 쉬움</span>
                </div>
                <div class="dash-card" style="min-height: auto; padding: 14px;">
                    <strong class="subtitle">🏆 완료 보상</strong>
                    <span class="desc" style="color: var(--color-accent); font-weight: var(--fw-h3);">+120 XP · 일몰 기록 배지</span>
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
            <div class="subtitle" style="color: var(--color-dark); cursor: pointer;" onclick="showToast('지도 기능을 준비 중입니다.')">지도 크게 보기 →</div>
        </div>
        <div class="map-layout">
            <div class="map">
                <div style="position: absolute; top: 12px; left: 12px; background: var(--color-white); padding: 6px 12px; border-radius: var(--radius-sm);" class="subtitle">
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
        <div class="grid" id="popularGrid"></div>
    </div>

    <div class="section">
        <div class="section-head">
            <div class="title">🆕 새로 올라온 퀘스트</div>
        </div>
        <div class="grid" id="newGrid"></div>
    </div>
</div>