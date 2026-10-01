<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<style>
    /* 여행 관리 전용 스타일 */
    .trip-header {
        display: flex;
        justify-content: space-between;
        align-items: flex-end;
        margin-bottom: 18px;
    }
    .trip-selector {
        border: 1px solid var(--color-border);
        background: var(--color-white);
        border-radius: var(--radius-md);
        padding: 12px 18px;
        min-width: 260px;
        cursor: pointer;
        display: flex;
        align-items: center;
        justify-content: space-between;
    }

    .calendar {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-lg);
        padding: 20px;
        margin-bottom: 20px;
        box-shadow: var(--shadow-card);
    }
    .calendar-tools { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; }
    .cal-nav-btn { display: flex; gap: 8px; align-items: center; }

    .toggle { display: flex; border: 1px solid var(--color-border); border-radius: var(--radius-sm); padding: 2px; background: var(--color-bg); }
    .toggle button { border: 0; background: transparent; padding: 6px 12px; border-radius: 6px; cursor: pointer; }
    .toggle button.active { background: var(--color-dark); color: var(--color-white); }

    .week-grid { display: grid; grid-template-columns: repeat(7, 1fr); gap: 8px; }
    .day {
        border: 1px solid var(--color-border2);
        border-radius: var(--radius-sm);
        padding: 10px;
        text-align: center;
        background: var(--color-white);
        cursor: pointer;
    }
    .day.selected { background: var(--color-dark); color: var(--color-white); border-color: var(--color-dark); }
    .day.selected .desc { color: var(--color-white); }

    .management-grid { display: grid; grid-template-columns: 1.2fr .8fr; gap: 16px; }
    .progressbar { height: 8px; background: var(--color-sub-bg); border-radius: 999px; overflow: hidden; margin: 12px 0; }
    .progressbar i { display: block; height: 100%; background: var(--color-dark); width: 0%; transition: width 0.3s; }

    .quest-row {
        display: flex;
        gap: 12px;
        align-items: center;
        padding: 14px 0;
        border-bottom: 1px solid var(--color-border2);
    }
    .quest-row:last-child { border-bottom: 0; }
    .quest-info { flex: 1; }

    .status {
        padding: 4px 8px;
        border-radius: 6px;
        background: var(--color-main);
        color: var(--color-dark);
    }
    .status.done { background: #EAFBF3; color: var(--color-success); }

    @media(max-width: 850px) {
        .management-grid { grid-template-columns: 1fr; }
    }
</style>

<!-- 상단 여행 선택 컨트롤 -->
<div class="trip-header">
    <div class="trip-selector" onclick="openTripPickerModal()">
        <div>
            <h2 id="currentTripName" class="title">부산 여행</h2>
            <span id="currentTripDates" class="desc">2026. 9. 22 - 9. 25</span>
        </div>
        <span class="subtitle">▾</span>
    </div>
    <button class="btn btn-secondary" onclick="switchTab('explorer', document.querySelectorAll('.main-tab-btn')[0])">＋ 퀘스트 추가</button>
</div>

<!-- 캘린더 영역 -->
<div class="calendar">
    <div class="calendar-tools">
        <div class="cal-nav-btn">
            <button class="small-btn" onclick="showToast('이전으로 이동')">‹</button>
            <span class="subtitle" id="periodLabel">2026. 9. 20 ~ 9. 26</span>
            <button class="small-btn" onclick="showToast('다음으로 이동')">›</button>
        </div>
        <div class="toggle">
            <button id="weekToggleBtn" class="desc active" onclick="switchCalendarMode('week')">주</button>
            <button id="monthToggleBtn" class="desc" onclick="switchCalendarMode('month')">월</button>
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
        <div style="font-size: var(--fs-b); font-weight: var(--fw-h1); margin-top: 10px; color: var(--color-dark);">
            <span id="xpTotal">0</span> XP
        </div>
        <div class="desc" style="margin-bottom: 16px;">이번 여행에서 획득한 경험치</div>

        <div style="border: 1px solid var(--color-border); border-radius: var(--radius-md); padding: 14px; background: var(--color-bg);">
            <strong class="subtitle" style="display: block; margin-bottom: 4px;">현재 목표</strong>
            <span class="desc">부산 퀘스트 10개 완료하기</span>
            <div class="progressbar"><i style="width: 40%;"></i></div>
            <span class="desc">4 / 10 완료</span>
        </div>
    </div>
</div>