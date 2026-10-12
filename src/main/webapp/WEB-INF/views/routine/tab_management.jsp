<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>

  <!-- 1. 통계 요약 카드 (3열 그리드) -->
  <div class="grid-12 mb-24">
    <div class="col-4 surface-gradient rounded-lg p-24 text-primary">
      <div class="text-sm color-white">선택 기간 달성률</div>
      <div id="progressRatio" class="text-2xl font-bold mt-8">0%</div>
    </div>
    <div class="col-4 surface-white border-default rounded-lg p-24">
      <div class="text-sm text-secondary">연속 실천일</div>
      <div class="text-2xl font-bold mt-8"><span id="userStreak">0</span><span
          class="text-sm text-secondary ml-auto">일</span></div>
    </div>
    <div class="col-4 surface-white border-default rounded-lg p-24">
      <div class="text-sm text-secondary">누적 XP</div>
      <div id="totalXp" class="text-2xl font-bold text-primary mt-8">0 XP</div>
    </div>
  </div>

  <!-- 2. 활성 루틴 선택 및 정보 -->
  <section class="surface-white border-default rounded-lg p-24 mb-24">
    <div class="flex items-center justify-between flex-wrap">
      <div class="flex items-center flex-wrap">
        <select id="activeRoutineSelect" onchange="App.routine.changeActiveRoutine(this.value)"
          class="form-select font-bold text-md rounded-md p-8"></select>
        <div class="text-xs text-secondary surface-sub-bg rounded-md px-16 py-8 mt-8">운영 기간 <b
            id="routinePeriodLabel"></b></div>
      </div>
      <div class="flex items-center gap-8 justify-end" style="width: 400px;">
        <button type="button" onclick="App.routine.openRoutineInfo()"
          class="btn btn-secondary text-sm font-bold rounded-md px-16 py-8">⚙ 기본 정보</button>
        <button type="button" onclick="App.routine.openRoutineCreateModal()"
          class="btn surface-dark color-white text-sm font-bold rounded-md px-16 py-8">＋ 새 루틴</button>
      </div>
    </div>
  </section>

  <!-- 3. 루틴 캘린더 위젯 -->
  <section class="surface-white border-default rounded-lg p-24 mb-24">
    <div class="flex items-center justify-between flex-wrap mb-16">
      <div>
        <h2 class="text-lg font-bold">📅 루틴 캘린더</h2>
        <p class="text-xs text-secondary mt-8">주/월 보기로 수행 계획과 완료 상태를 확인하세요.</p>
      </div>
      <div class="flex items-center">
        <div class="flex items-center mr-auto">
          <button type="button" onclick="App.routine.moveCalendar(-1)"
            class="btn btn-secondary rounded-md p-8 font-bold">‹</button>
          <button type="button" onclick="App.routine.moveCalendar(1)"
            class="btn btn-secondary rounded-md p-8 font-bold">›</button>
        </div>
        <button type="button" id="calendarViewBtn" onclick="App.routine.toggleCalendarView()"
          class="btn surface-dark color-white rounded-md p-8 font-bold">주</button>
      </div>
    </div>
    <div class="flex items-center justify-between mb-16">
      <div id="calendarTitle" class="text-lg font-bold"></div>
      <button type="button" onclick="App.routine.calendarToday()" class="badge border-none">오늘</button>
    </div>
    <div id="calendarGrid" class="cal-grid"></div>
  </section>

  <!-- 4. 타임테이블 및 Task 설정 (8:4 12컬럼 그리드) -->
  <div class="grid-12 items-start">
    <!-- 타임테이블 (8컬럼) -->
    <section class="col-8 surface-white border-default rounded-lg p-24">
      <div class="flex items-center justify-between mb-16 pb-16 border-bottom">
        <div>
          <h2 class="text-lg font-bold">오늘의 루틴 타임테이블</h2>
          <p id="selectedDateLabel" class="text-xs text-secondary mt-8"></p>
        </div>
      </div>
      <div class="timeline-table mt-16">
        <div class="tt-head">
          <div>#</div>
          <div>시간</div>
          <div>오늘 해야 할 루틴</div>
          <div>상태</div>
        </div>
        <div id="manageTimetable"></div>
      </div>
    </section>

    <!-- Task 설정 (4컬럼, 상단 sticky) -->
    <aside class="col-4 sticky task-form-container">
      <!-- 폼 헤더 -->
      <div class="task-form-header">
        <h2 class="task-form-title">Task 설정</h2>
        <button type="button" onclick="App.routine.openTaskForm()" class="task-form-add-btn">＋ 추가</button>
      </div>

      <!-- 메인 폼 영역 -->
      <form id="taskForm" class="task-form" onsubmit="App.routine.saveTask(event)">
        <input type="hidden" id="editingTaskId">

        <!-- 루틴 선택 -->
        <div class="task-form-group">
          <label class="task-form-label" for="taskRoutine">루틴</label>
          <select id="taskRoutine" required class="task-form-select"></select>
        </div>

        <!-- 루틴 내용 -->
        <div class="task-form-group">
          <label class="task-form-label" for="taskTitle">루틴 내용</label>
          <input id="taskTitle" required class="task-form-input" placeholder="루틴 내용을 입력하세요">
        </div>

        <!-- 시간 & 알림 2열 배치 -->
        <div class="task-form-row">
          <div class="task-form-group">
            <label class="task-form-label" for="taskTime">시간</label>
            <input id="taskTime" type="time" required class="task-form-time">
          </div>
          <div class="task-form-group">
            <label class="task-form-label" for="taskAlarm">알림</label>
            <input id="taskAlarm" type="time" class="task-form-time">
          </div>
        </div>

        <!-- 카테고리 & 인증방식 2열 배치 -->
        <div class="task-form-row">
          <div class="task-form-group">
            <label class="task-form-label" for="taskCat">카테고리</label>
            <select id="taskCat" required class="task-form-category"></select>
          </div>
        </div>

        <!-- 장소: 검색해서 선택하면 완료할 때 위치 인증 -->
        <div class="task-form-group">
          <label class="task-form-label" for="taskPlaceSearch">장소 (선택하면 완료 시 위치 인증)</label>
          <input type="hidden" id="taskPlaceId">
          <div style="display:flex; gap:8px;">
            <input id="taskPlaceSearch" class="task-form-input" placeholder="장소 이름 검색 (예: 헬튼 피트니스)"
              onkeydown="if (event.key === 'Enter') { event.preventDefault(); App.routine.searchTaskPlace(); }">
            <button type="button" class="task-form-add-btn" onclick="App.routine.searchTaskPlace()">검색</button>
          </div>
          <div id="taskPlaceResults" class="is-hidden" style="max-height:200px; overflow-y:auto; margin-top:8px;"></div>
          <div id="taskPlaceSelected" class="task-form-label-row is-hidden" style="margin-top:8px;">
            <span id="taskPlaceName" class="task-form-label"></span>
            <button type="button" class="task-form-delete-btn" onclick="App.routine.clearTaskPlace()">선택 해제</button>
          </div>

          <!-- 장소를 고르면 나타나는 인증 반경 -->
          <div id="radiusControl" class="task-form-radius-control is-hidden">
            <div class="task-form-radius-label-row">
              <span>허용 인증 반경</span>
              <span id="radiusValueLabel">50m</span>
            </div>
            <input type="range" id="taskRadius" min="50" max="1000" step="50" value="50" class="task-form-range"
              oninput="document.getElementById('radiusValueLabel').textContent = this.value + 'm'">
          </div>
        </div>

        <!-- 반복 설정 -->
        <div class="task-form-group">
          <label class="task-form-label" for="taskRepeat">반복</label>
          <select id="taskRepeat" class="task-form-select" onchange="App.routine.onRepeatChange(this.value)">
            <option value="DAILY">매일 반복</option>
            <option value="WEEKDAY">평일 반복</option>
            <option value="WEEKEND">주말 반복</option>
            <option value="CUSTOM">사용자 지정</option>
            <option value="ONCE">1회성</option>
          </select>
          <div id="repeatDays" class="task-form-label-row is-hidden">
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="MON"> 월</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="TUE"> 화</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="WED"> 수</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="THU"> 목</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="FRI"> 금</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="SAT"> 토</label>
            <label class="task-form-checkbox-label"><input type="checkbox" name="taskDays" value="SUN"> 일</label>
          </div>
          <input id="taskOnceDate" type="date" class="task-form-input is-hidden">
        </div>

        <!-- 메모 입력 -->
        <div class="task-form-group">
          <label class="task-form-label" for="taskMemo">메모</label>
          <textarea id="taskMemo" class="task-form-textarea" rows="3" placeholder="메모를 입력하세요"></textarea>
        </div>

        <!-- 하단 버튼 영역 -->
        <div class="task-form-btns">
          <button type="submit" class="task-form-submit-btn">저장</button>
          <button type="button" onclick="App.routine.deleteActiveTask()" class="task-form-delete-btn">삭제</button>
        </div>
      </form>
    </aside>
  </div>