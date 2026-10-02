<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 상단 여행 선택 컨트롤 -->
<div class="trip-header">
    <div class="trip-selector" onclick="App.quest.openTripPickerModal()">
        <div>
            <h2 id="currentTripName" class="title">부산 여행</h2>
            <span id="currentTripDates" class="desc">2026. 9. 22 - 9. 25</span>
        </div>
        <span class="subtitle">▾</span>
    </div>
    <button class="btn btn-secondary" onclick="App.quest.switchTab('explorer', document.querySelectorAll('.main-tab-btn')[0])">＋ 퀘스트 추가</button>
</div>

<!-- 캘린더 영역 -->
<div class="calendar">
    <div class="calendar-tools">
        <div class="cal-nav-btn">
            <button class="small-btn" onclick="App.quest.moveQuestCalendar(-1)">‹</button>
            <span class="subtitle" id="periodLabel">2026. 9. 20 ~ 9. 26</span>
            <button class="small-btn" onclick="App.quest.moveQuestCalendar(1)">›</button>
        </div>
        <div class="toggle">
            <button id="weekToggleBtn" class="desc active" onclick="App.quest.switchCalendarMode('week')">주</button>
            <button id="monthToggleBtn" class="desc" onclick="App.quest.switchCalendarMode('month')">월</button>
        </div>
    </div>
    <div id="calendarBody">
        <div class="week-grid">
            <div class="day"><div class="desc">일</div><span class="subtitle">20</span></div>
            <div class="day"><div class="desc">월</div><span class="subtitle">21</span></div>
            <div class="day selected"><div class="desc">화</div><span class="subtitle">22</span></div>
            <div class="day"><div class="desc">수</div><span class="subtitle">23</span></div>
            <div class="day"><div class="desc">목</div><span class="subtitle">24</span></div>
            <div class="day"><div class="desc">금</div><span class="subtitle">25</span></div>
            <div class="day"><div class="desc">토</div><span class="subtitle">26</span></div>
        </div>
    </div>
</div>

<!-- 메인 관리 패널 -->
<div class="management-grid">
    <!-- 오늘의 일정 패널 -->
    <div class="dash-card">
        <div class="dash-card-header">
            <div>
                <div class="title">오늘의 일정</div>
                <div class="desc"><span id="doneCount">0</span> / <span id="totalCount">0</span> 완료</div>
            </div>
        </div>
        <div class="progressbar"><i id="progressBar"></i></div>
        <div id="todayList">
            <!-- 자바스크립트에 의해 동적 렌더링 -->
        </div>
    </div>

    <!-- 진행도 및 대시보드 -->
    <div class="dash-card">
        <div class="title">🎒 여행 진행도</div>
        <div class="quest-detail-13">
            <span id="xpTotal">0</span> XP
        </div>
        <div class="quest-detail-14 desc">이번 여행에서 획득한 경험치</div>

        <div class="quest-detail-15">
            <strong class="quest-detail-16 subtitle">현재 목표</strong>
            <span class="desc">부산 퀘스트 10개 완료하기</span>
            <div class="progressbar"><i class="quest-detail-17"></i></div>
            <span class="desc">4 / 10 완료</span>
        </div>
    </div>
</div>
