<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<!-- 통계 요약 카드 -->
<div class="layout-grid columns-1 medium-columns-3 ds-gap-4">
  <div class="surface-gradient radius-lg ds-p-5 color-white">
    <div class="type-sm color-main">선택 기간 달성률</div>
    <div id="progressRatio" class="type-3xl weight-heading ds-mt-1">0%</div>
  </div>
  <div class="surface-white border-default border-color-border radius-lg ds-p-5">
    <div class="type-sm color-muted">연속 실천일</div>
    <div class="type-3xl weight-heading ds-mt-1">12<span class="type-base color-muted ds-ml-1">일</span></div>
  </div>
  <div class="surface-white border-default border-color-border radius-lg ds-p-5">
    <div class="type-sm color-muted">누적 XP</div>
    <div id="totalXp" class="type-3xl weight-heading color-dark ds-mt-1">1,240 XP</div>
  </div>
</div>

<!-- 활성 루틴 선택 및 정보 -->
<section class="surface-white border-default border-color-border radius-lg ds-p-5 shadow-card">
  <div class="layout-row layout-column large-layout-horizontal ds-gap-4 large-align-center justify-between">
    <div class="layout-row layout-column medium-layout-horizontal medium-align-center ds-gap-3">
      <select id="activeRoutineSelect" onchange="App.routine.changeActiveRoutine(this.value)" class="surface-sub-bg border-default border-color-border type-lg weight-heading radius-md ds-px-4 ds-py-3 ds-min-w-260px"></select>
      <div class="type-xs color-muted surface-sub-bg radius-md ds-px-3 ds-py-2">운영 기간 <b id="routinePeriodLabel"></b></div>
    </div>
    <div class="layout-row ds-gap-2">
      <button type="button" onclick="App.routine.openRoutineInfo()" class="ds-px-4 ds-py-2 surface-sub-bg color-text weight-bold radius-md type-sm">⚙ 기본 정보</button>
      <button type="button" onclick="App.routine.openRoutineCreateModal()" class="ds-px-4 ds-py-2 surface-main color-dark weight-bold radius-md type-sm">＋ 새 루틴</button>
    </div>
  </div>
</section>

<!-- 루틴 캘린더 -->
<section class="surface-white border-default border-color-border radius-lg ds-p-4 medium-ds-p-5 shadow-card">
  <div class="layout-row layout-column medium-layout-horizontal medium-align-center justify-between ds-gap-3">
    <div>
      <h2 class="weight-heading type-lg">📅 루틴 캘린더</h2>
      <p class="type-xs color-muted ds-mt-1">주/월 보기로 수행 계획과 완료 상태를 확인하세요.</p>
    </div>
    <div class="layout-row align-center ds-gap-2">
      <div class="layout-row ds-gap-1">
        <button type="button" onclick="App.routine.moveCalendar(-1)" class="ds-w-9 ds-h-9 radius-sm surface-sub-bg weight-heading">‹</button>
        <button type="button" onclick="App.routine.moveCalendar(1)" class="ds-w-9 ds-h-9 radius-sm surface-sub-bg weight-heading">›</button>
      </div>
      <button type="button" id="calendarViewBtn" onclick="App.routine.toggleCalendarView()" class="ds-px-4 ds-py-2 radius-md surface-main color-dark type-xs weight-heading">주</button>
    </div>
  </div>
  <div class="layout-row align-center justify-between ds-mt-4 ds-mb-3">
    <div id="calendarTitle" class="weight-heading type-lg"></div>
    <button type="button" onclick="App.routine.calendarToday()" class="type-xs weight-bold color-dark">오늘</button>
  </div>
  <div id="calendarGrid" class="cal-grid"></div>
</section>

<!-- 타임테이블 및 Task 설정 -->
<div class="layout-grid columns-1 large-columns-12 ds-gap-6 align-start">
  <section class="large-span-8 surface-white border-default border-color-border radius-lg ds-p-4 medium-ds-p-5 shadow-card">
    <div class="layout-row layout-column medium-layout-horizontal medium-align-center justify-between ds-gap-3 ds-pb-3 border-bottom border-color-sub-bg">
      <div>
        <h2 class="weight-heading type-lg">오늘의 루틴 타임테이블</h2>
        <p id="selectedDateLabel" class="type-xs color-muted ds-mt-1"></p>
      </div>
    </div>
    <div class="timeline-table ds-mt-4">
      <div class="tt-head">
        <div>#</div><div>시간</div><div>오늘 해야 할 루틴</div><div>상태</div>
      </div>
      <div id="manageTimetable"></div>
    </div>
  </section>

  <aside class="large-span-4 surface-white border-default border-color-border radius-lg ds-p-5 shadow-card large-position-sticky large-ds-top-24">
    <div class="layout-row justify-between align-center ds-pb-3 border-bottom border-color-sub-bg">
      <h2 class="weight-heading">Task 설정</h2>
      <button type="button" onclick="App.routine.openTaskForm()" class="ds-px-3 ds-py-2 surface-dark color-white radius-sm type-xs weight-bold">＋ 추가</button>
    </div>
    <form id="taskForm" class="space-y-4 ds-mt-4" onsubmit="App.routine.saveTask(event)">
      <input type="hidden" id="editingTaskId">
      <div>
        <label class="type-xs weight-bold" for="taskTitle">루틴 내용</label>
        <input id="taskTitle" required class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3 type-sm">
      </div>
      <div class="layout-grid columns-2 ds-gap-3">
        <div>
          <label class="type-xs weight-bold" for="taskTime">시간</label>
          <input id="taskTime" type="time" required class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3">
        </div>
        <div>
          <label class="type-xs weight-bold" for="taskCat">카테고리</label>
          <select id="taskCat" class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-2 ds-py-3 type-xs">
            <option>💪 운동</option><option>💧 건강</option><option>📚 자기계발</option><option>🧠 공부</option><option>💼 업무</option><option>🏠 일상</option>
          </select>
        </div>
      </div>
      <div>
        <label class="type-xs weight-bold" for="taskLocation">장소</label>
        <input id="taskLocation" class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3 type-sm" placeholder="예: 집 / 헬스장 / 광안리">
      </div>
      <div>
        <label class="type-xs weight-bold" for="taskRepeat">반복</label>
        <select id="taskRepeat" class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3 type-sm">
          <option>매일 반복</option><option>평일 반복</option><option>주말 반복</option><option>사용자 지정</option><option>1회성</option>
        </select>
      </div>
      <div>
        <label class="type-xs weight-bold" for="taskAlarm">알림</label>
        <input id="taskAlarm" type="time" class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3">
      </div>
      <div>
        <label class="type-xs weight-bold" for="taskMemo">메모</label>
        <textarea id="taskMemo" class="width-full ds-mt-1 surface-sub-bg border-default border-color-border radius-md ds-px-3 ds-py-3 type-sm" rows="3"></textarea>
      </div>
      <div class="layout-row ds-gap-2">
        <button type="submit" class="layout-grow surface-dark color-white weight-bold radius-md ds-py-3">저장</button>
        <button type="button" onclick="App.routine.deleteActiveTask()" class="ds-px-4 border-default border-color-danger-bg color-danger radius-md">삭제</button>
      </div>
    </form>
  </aside>
</div>