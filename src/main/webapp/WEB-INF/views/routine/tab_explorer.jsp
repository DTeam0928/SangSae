<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 상단 배너 & 검색 -->
<section class="surface-gradient color-white radius-lg ds-p-6 medium-ds-p-8 shadow-card">
  <div class="max-width-3xl">
    <div class="type-xs weight-heading letter-spacing-wide color-main">PLACE MAKES HABIT</div>
    <h1 class="type-3xl medium-type-4xl weight-heading ds-mt-2 color-text">장소가 습관을 만든다.</h1>
    <p class="ds-mt-3 type-sm medium-type-base color-main line-height-relaxed">루틴을 검색하는 것에서 끝나지 않습니다. 지금 있는 장소와 주변 스팟, 사람들이 실제로 수행하는 루틴을 연결해 오늘의 행동을 찾아보세요.</p>
  </div>
  <div class="ds-mt-6 layout-row layout-column medium-layout-horizontal ds-gap-3">
    <div class="position-relative layout-grow">
      <span class="position-absolute ds-left-4 ds-top-3_5">🔍</span>
      <input id="routineSearch" oninput="App.routine.renderExplore()" class="width-full radius-md ds-pl-11 ds-pr-10 ds-py-3 color-text surface-white border-none focus:outline-none" placeholder="루틴, 장소, 헬스장, 카페, 테마 검색">
      <button type="button" onclick="App.routine.clearSearch()" class="position-absolute ds-right-3 ds-top-3 color-muted">×</button>
    </div>
    <button type="button" onclick="App.routine.locateUser()" class="ds-px-5 ds-py-3 radius-md surface-soft border-default border-color-white weight-bold text-nowrap">📍 내 위치 찾기</button>
  </div>
  <div class="ds-mt-3 layout-row align-center ds-gap-2 type-xs color-main">
    <span id="locationStatus">기본 위치: 서울 시청 인근 · GPS를 사용하면 주변 추천이 정교해집니다.</span>
  </div>
</section>

<!-- 카테고리 필터 및 정렬 -->
<section class="surface-white border-default border-color-border radius-lg ds-p-4 medium-ds-p-5 shadow-card">
  <div class="layout-row layout-column large-layout-horizontal ds-gap-3 large-align-center">
    <div class="layout-row ds-gap-2 scroll-x scrollbar ds-pb-1 layout-grow">
      <button type="button" class="day-chip active" data-cat="전체" onclick="App.routine.setCategory('전체',this)">🔥 전체</button>
      <button type="button" class="day-chip" data-cat="운동" onclick="App.routine.setCategory('운동',this)">💪 운동</button>
      <button type="button" class="day-chip" data-cat="미라클 모닝" onclick="App.routine.setCategory('미라클 모닝',this)">🌅 미라클 모닝</button>
      <button type="button" class="day-chip" data-cat="공부" onclick="App.routine.setCategory('공부',this)">📚 공부</button>
      <button type="button" class="day-chip" data-cat="건강" onclick="App.routine.setCategory('건강',this)">🥗 건강</button>
      <button type="button" class="day-chip" data-cat="숙면" onclick="App.routine.setCategory('숙면',this)">😴 숙면</button>
      <button type="button" class="day-chip" data-cat="직장인" onclick="App.routine.setCategory('직장인',this)">💼 직장인</button>
    </div>
    <select id="routineSort" onchange="App.routine.renderExplore()" class="surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3 type-sm">
      <option value="near">내 위치 가까운 순</option>
      <option value="popular">인기순</option>
      <option value="new">최신순</option>
      <option value="easy">부담 적은 순</option>
    </select>
  </div>
</section>

<!-- 검색 결과 섹션 -->
<section id="searchState" class="is-hidden">
  <div class="layout-row justify-between align-end ds-mb-3">
    <div>
      <h2 class="weight-heading type-xl">검색 결과</h2>
      <p id="searchCount" class="type-sm color-muted ds-mt-1"></p>
    </div>
    <button type="button" onclick="App.routine.clearSearch()" class="type-sm color-dark weight-bold">탐색 홈으로</button>
  </div>
  <div id="searchGrid" class="layout-grid columns-1 medium-columns-2 large-columns-3 ds-gap-5"></div>
</section>

<!-- 탐색 홈 컨텐츠 -->
<div id="exploreHome" class="space-y-8">
  <section class="surface-white border-default border-color-border radius-lg ds-p-5 shadow-card">
    <div class="layout-row layout-column large-layout-horizontal large-align-end justify-between ds-gap-4 ds-mb-4">
      <div>
        <h2 class="type-xl weight-heading">📍 지금 내 주변 루틴 스팟</h2>
        <p class="type-xs color-muted ds-mt-1">GPS 기준으로 가까운 장소와 그곳에서 많이 수행되는 루틴을 연결합니다.</p>
      </div>
      <div class="layout-row ds-gap-2">
        <button type="button" onclick="App.routine.focusUser()" class="ds-px-3 ds-py-2 radius-md surface-main color-dark type-xs weight-bold">내 위치로 이동</button>
        <button type="button" onclick="App.routine.openPlaceFilters()" class="ds-px-3 ds-py-2 radius-md surface-sub-bg color-muted type-xs weight-bold">필터</button>
      </div>
    </div>
    <div id="map" class="map-wrap" style="position: relative; overflow: hidden;">
      <div id="customMapContainer" style="width: 100%; height: 100%;"></div>
      <div class="position-absolute ds-bottom-3 ds-left-3 surface-glass backdrop-blur-sm ds-px-3 ds-py-1_5 radius-sm type-xs weight-bold shadow-card border-default border-color-border color-muted ds-z-10 layout-row align-center ds-gap-2" style="pointer-events: none;">
        <span class="ds-w-2 ds-h-2 radius-pill surface-dark animate-pulse"></span> SANGSAE 루틴 스팟 지도
      </div>
    </div>
    <div id="nearbyPlaces" class="layout-grid columns-1 medium-columns-3 ds-gap-3 ds-mt-4"></div>
  </section>

  <section>
    <div class="layout-row justify-between align-end ds-mb-3">
      <div>
        <h2 class="weight-heading type-xl">🔥 지금 이 근처에서 뜨는 루틴</h2>
        <p class="type-xs color-muted ds-mt-1">내 주변 장소에서 최근 많이 시작된 루틴</p>
      </div>
      <button type="button" onclick="App.routine.showSection('nearby')" class="type-xs color-muted">더보기 →</button>
    </div>
    <div id="nearbyRoutineGrid" class="layout-grid columns-1 medium-columns-2 large-columns-3 ds-gap-5"></div>
  </section>

  <section class="surface-white border-default border-color-border radius-lg ds-p-5 shadow-card">
    <h2 class="weight-heading type-xl">🎯 일상 루틴 가이드</h2>
    <p class="type-xs color-muted ds-mt-1">마음에 드는 루틴을 담아 내 일정으로 만들고, 지정된 장소나 시간에서 완료해 보세요.</p>
  </section>

  <section>
    <div class="layout-row justify-between align-end ds-mb-3">
      <div>
        <h2 class="weight-heading type-xl">💡 당신에게 추천</h2>
        <p class="type-xs color-muted ds-mt-1">저장·완료 기록과 현재 위치를 함께 반영한 추천</p>
      </div>
    </div>
    <div id="recommendedGrid" class="layout-grid columns-1 medium-columns-2 large-columns-3 ds-gap-5"></div>
  </section>

  <section>
    <div class="layout-row justify-between align-end ds-mb-3">
      <div>
        <h2 class="weight-heading type-xl">🔥 전국 인기 루틴</h2>
        <p class="type-xs color-muted ds-mt-1">장소와 함께 많이 저장된 루틴</p>
      </div>
    </div>
    <div id="popularGrid" class="layout-grid columns-1 medium-columns-2 large-columns-3 ds-gap-5"></div>
  </section>

  <section>
    <div class="layout-row justify-between align-end ds-mb-3">
      <div>
        <h2 class="weight-heading type-xl">🆕 새로 올라온 루틴</h2>
        <p class="type-xs color-muted ds-mt-1">최근 등록된 장소 기반 루틴</p>
      </div>
    </div>
    <div id="newGrid" class="layout-grid columns-1 medium-columns-2 large-columns-3 ds-gap-5"></div>
  </section>
</div>
