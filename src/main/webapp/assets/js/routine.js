(() => {
'use strict';

// ============================================================
// QUESTRIP ROUTINE - SPA & GLOBAL SCRIPTS (순수 DB 연동 버전)
// ============================================================

const contextPath = document.body.dataset.contextPath || '';

// 더미 배열 전면 삭제: 오직 서버 DB에서 가져온 데이터만 사용
let routines = [];
let serverTimelineTasks = [];
let activeRoutineId = null;
let activeUserRoutineServerId = null;
let totalXp = 0;
let userStreak = 0;

let routineMap = null;
let nearbyRoutinePlaces = [];

let routineCategory = '전체';
let selectedRoutine = null;
let selectedDate = App.dateKey();
let calendarCursor = new Date(selectedDate + 'T00:00:00');
let calendarView = 'week';
let userLocation = { lat: 35.1434, lng: 129.0346 };
let radiusKm = 3;

const CATEGORY_LABEL_MAP = {
    'EXERCISE': '운동',
    'STUDY': '공부',
    'HEALTH': '건강',
    'MIRACLE_MORNING': '미라클 모닝',
    'SLEEP': '숙면',
    'WORKER': '직장인',
    'DAILY': '일상',
    'GROWTH': '성장'
};

function formatCategoryName(code) {
    if (!code) return '일상';
    const upper = String(code).trim().toUpperCase();
    return CATEGORY_LABEL_MAP[upper] || code;
}

function toast(m, icon = '✨') {
    const e = document.getElementById('toast');
    if (!e) return;
    e.textContent = icon + ' ' + m;
    e.classList.add('show');
    clearTimeout(window.__toast);
    window.__toast = setTimeout(() => e.classList.remove('show'), 2500);
}

function hide(id) { const el = document.getElementById(id); if (el) el.classList.remove('show'); }
function closeModal(id, e) { if (e.target.id === id) hide(id); }

/* 루틴 탭 전환 (탐색 / 관리) */
function switchTab(id, btn) {
    // 1. 모든 탭 콘텐츠 비활성화 및 대상 탭(#tab-explore, #tab-manage) 활성화
    document.querySelectorAll('.tab-content').forEach(el => {
        el.classList.remove('active');
    });

    // 'explorer' -> 'explore', 'management' -> 'manage' 대응
    const targetId = id === 'explorer' ? 'explore' : (id === 'management' ? 'manage' : id);
    const targetTab = document.getElementById('tab-' + targetId);
    if (targetTab) {
        targetTab.classList.add('active');
    }

    // 2. 모든 탭 버튼 비활성화 및 선택된 버튼 활성화
    document.querySelectorAll('.main-tab-btn').forEach(el => {
        el.classList.remove('active');
    });

    const targetBtn = btn || (targetId === 'explore' ? document.getElementById('tabExplore') : document.getElementById('tabManage'));
    if (targetBtn) {
        targetBtn.classList.add('active');
    }

    // 3. 탭 전환 후 후속 동작 (관리 탭: 렌더링 / 탐색 탭: 카카오 지도 relayout)
    if (targetId === 'manage') {
        renderManage();
    } else if (targetId === 'explore' && routineMap) {
        setTimeout(() => {
            routineMap.relayout();
            if (userLocation && typeof kakao !== 'undefined' && kakao.maps) {
                routineMap.setCenter(new kakao.maps.LatLng(userLocation.lat, userLocation.lng));
            }
        }, 50);
    }
}

function toggleLocationRadius(checked) {
    const el = document.getElementById('radiusControl');
    if (el) el.classList.toggle('is-hidden', !checked);
}

/* 1. 서버 루틴 카탈로그 동기화 (DB 조회만 허용) */
function fetchRoutinesFromServer() {
    fetch(contextPath + '/routines')
        .then(res => res.ok ? res.json() : null)
        .then(json => {
            if (json && json.success && Array.isArray(json.data)) {
                routines = json.data.map(item => ({
                    id: String(item.id),
                    serverId: item.id,
                    name: item.title,
                    desc: item.description || '',
                    cat: [formatCategoryName(item.category || '일상')],
                    popular: item.adoptedCount || 0,
                    difficulty: 2,
                    tasks: []
                }));
            } else {
                routines = [];
            }
            renderExplore();
        })
        .catch(() => {
            routines = [];
            renderExplore();
        });
}

document.addEventListener('DOMContentLoaded', function() {
    fetchRoutinesFromServer();
    loadManage();
});

function filteredRoutines() {
    const q = document.getElementById('routineSearch').value.trim().toLowerCase();
    return routines.filter(r => (routineCategory === '전체' || (r.cat && r.cat.includes(routineCategory))) && (!q || (r.name + ' ' + r.desc).toLowerCase().includes(q)));
}

function routineCard(r) {
  const catHtml = (r.cat || []).map(function(c) { return '<span class="pill">' + App.escape(c) + '</span>'; }).join('');

  return '<article class="card surface-white border-default border-color-border radius-lg clip-overflow cursor-pointer" onclick="App.routine.openRoutine(\'' + r.id + '\')">' +
         '<div class="ds-h-28 surface-gradient ds-p-4 layout-row justify-between">' +
         '<div class="layout-row ds-gap-1 layout-wrap align-start">' + catHtml + '</div>' +
         '</div>' +
         '<div class="ds-p-5">' +
         '<h3 class="weight-heading type-lg">' + App.escape(r.name) + '</h3>' +
         '<p class="type-sm color-muted ds-mt-2 text-clamp-two">' + App.escape(r.desc) + '</p>' +
         '<div class="layout-row justify-between align-center ds-mt-4 ds-pt-3 border-top border-color-sub-bg type-xs color-muted">' +
         '<span>담은 인원: ' + (r.popular || 0) + '명</span>' +
         '<button type="button" class="btn btn-secondary ds-px-3 ds-py-1" onclick="event.stopPropagation();App.routine.adoptRoutineToServer(' + r.serverId + ')">내 루틴에 담기</button>' +
         '</div>' +
         '</div>' +
         '</article>';
}

function renderExplore() {
    const a = filteredRoutines(), q = document.getElementById('routineSearch').value.trim();
    document.getElementById('searchState').classList.toggle('is-hidden', !q);
    document.getElementById('exploreHome').classList.toggle('is-hidden', !!q);

    const emptyMsg = '<div class="empty span-full" style="padding: 40px; text-align: center; color: var(--color-muted);">DB에 등록된 루틴 템플릿이 없습니다.</div>';

    if (q) {
        document.getElementById('searchCount').textContent = '"' + q + '" 검색 결과 ' + a.length + '개';
        document.getElementById('searchGrid').innerHTML = a.length ? a.map(routineCard).join('') : emptyMsg;
        return;
    }

    const cardsHtml = a.length ? a.map(routineCard).join('') : emptyMsg;
    document.getElementById('nearbyRoutineGrid').innerHTML = cardsHtml;
    document.getElementById('recommendedGrid').innerHTML = cardsHtml;
    document.getElementById('popularGrid').innerHTML = cardsHtml;
    document.getElementById('newGrid').innerHTML = cardsHtml;
}

function clearSearch() { document.getElementById('routineSearch').value = ''; renderExplore(); }
function setCategory(c, b) {
    routineCategory = c;
    document.querySelectorAll('[data-cat]').forEach(x => x.classList.remove('active'));
    b.classList.add('active');
    renderExplore();
    searchNearbyRoutinePlaces();
}

function openRoutine(id) {
    selectedRoutine = routines.find(r => r.id === id);
    if (!selectedRoutine) return;
    document.getElementById('rmTitle').textContent = selectedRoutine.name;
    document.getElementById('rmDesc').textContent = selectedRoutine.desc;
    document.getElementById('rmBadges').innerHTML = (selectedRoutine.cat || []).map(function(c) { return '<span class="pill">' + App.escape(c) + '</span>'; }).join('');
    document.getElementById('rmPlace').textContent = '추천 장소';
    document.getElementById('rmFit').textContent = '100%';
    document.getElementById('rmUsers').textContent = (selectedRoutine.popular || 0) + '명';
    document.getElementById('rmTasks').innerHTML = '<div class="desc" style="padding:15px; text-align:center;">이 루틴을 담아 나만의 일상을 시작해보세요!</div>';
    document.getElementById('routineModal').classList.add('show');
}

function toggleModalTasks(c) { document.querySelectorAll('.rm-check').forEach(x => x.checked = c.checked); }

/* 템플릿 Fork (내 루틴으로 담기: POST /user-routines/adopt) */
function adoptRoutineToServer(templateId) {
    if (!templateId) return;
    const body = new URLSearchParams({
        routineTemplateId: String(templateId),
        startDate: App.dateKey()
    });
    fetch(contextPath + '/user-routines/adopt', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: body
    })
    .then(res => res.json())
    .then(json => {
        if (json && json.success) {
            toast('루틴을 내 일상에 추가했습니다!', '✨');
            hide('routineModal');
            switchTab('manage');
            loadManage(json.data && json.data.id);
        } else {
            toast(json.message || '루틴 담기에 실패했습니다.', '⚠️');
        }
    })
    .catch(() => {
        toast('루틴 담기 중 통신 오류가 발생했습니다.', '⚠️');
    });
}

function addAllRoutineTasks() {
    if (selectedRoutine && selectedRoutine.serverId) {
        adoptRoutineToServer(selectedRoutine.serverId);
    }
}
function addSelectedRoutineTasks() { addAllRoutineTasks(); }

function locateUser() {
    if (!navigator.geolocation) { toast('GPS를 지원하지 않는 브라우저입니다.'); return; }
    toast('현재 위치를 확인하는 중입니다…', '🧭');
    navigator.geolocation.getCurrentPosition(pos => {
        userLocation = { lat: pos.coords.latitude, lng: pos.coords.longitude };
        const status = document.getElementById('locationStatus');
        if (status) status.textContent = 'GPS 위치 사용 중 (' + userLocation.lat.toFixed(4) + ', ' + userLocation.lng.toFixed(4) + ')';
        if (routineMap && typeof MapUtil !== 'undefined') {
            MapUtil.moveTo(routineMap, userLocation.lat, userLocation.lng);
        }
        searchNearbyRoutinePlaces();
        toast('현재 위치를 반영했습니다.', '📍');
    }, () => toast('위치 권한을 확인하지 못했습니다.', '⚠️'), { enableHighAccuracy: true, timeout: 8000, maximumAge: 0 });
}

function focusUser() {
    locateUser();
}

function openPlaceFilters() {
    const cur = prompt('검색할 주변 루틴 스팟 키워드를 입력하세요:\n(예: 운동, 러닝, 공원, 카페, 도서관, 헬스장)', '운동');
    if (cur && cur.trim()) {
        searchNearbyRoutinePlaces(cur.trim());
        toast(`'${cur.trim()}' 스팟을 지도에 표시합니다.`, '🔍');
    }
}

/* ============================================================
   루틴 관리 탭 (백엔드: /api/my-routines, 문서 docs/ROUTINE_MANAGE_API.md)
   ============================================================ */
const MY_API = contextPath + '/api/my-routines';
let myRoutines = [];          // 내 루틴 (드롭다운)
let manageCategories = [];    // Task·공개 카테고리 선택지
let timelineTasks = [];       // 선택한 날짜의 타임테이블
let taskPlaceResults = [];    // 장소 검색 결과

/* fetch 공통: 비로그인이면 401 JSON 을 받도록 헤더를 붙인다. params(URLSearchParams)가 있으면 form 전송 */
function api(method, url, params) {
    const opt = { method: method, headers: { 'X-Requested-With': 'XMLHttpRequest' } };
    if (params) opt.body = params;
    return fetch(url, opt).then(res => res.json());
}
function fail(json, fallback) { toast((json && json.message) || fallback, '⚠️'); }
function setVal(id, v) { const el = document.getElementById(id); if (el) el.value = v; }
function emptyBox(msg) { return '<div class="empty" style="padding: 30px; text-align: center; color: var(--color-muted);">' + msg + '</div>'; }
function activeRoutine() { return myRoutines.find(r => r.id === activeRoutineId) || null; }

/* 관리 탭 데이터 불러오기. selectId 를 주면 그 루틴을 선택 상태로 */
function loadManage(selectId) {
    return api('GET', MY_API).then(json => {
        if (!json.success) return fail(json, '루틴 정보를 불러오지 못했습니다.');
        myRoutines = json.data.routines;
        manageCategories = json.data.categories;
        if (selectId) activeRoutineId = Number(selectId);
        if (!activeRoutine()) activeRoutineId = myRoutines.length ? myRoutines[0].id : null;
        renderRoutineSelects();
        renderCategoryOptions();
        openTaskForm();
        renderManage();
    }).catch(() => toast('루틴 정보를 불러오는 중 오류가 발생했습니다.', '⚠️'));
}

/* 활성 루틴 드롭다운 + Task 폼의 루틴 선택 */
function renderRoutineSelects() {
    const options = myRoutines.map(r => '<option value="' + r.id + '">' + App.escape(r.title) + '</option>').join('');
    const sel = document.getElementById('activeRoutineSelect');
    if (sel) {
        sel.innerHTML = options || '<option value="">아직 루틴이 없습니다</option>';
        if (activeRoutineId) sel.value = String(activeRoutineId);
    }
    const taskSel = document.getElementById('taskRoutine');
    if (taskSel) {
        taskSel.innerHTML = options;
        if (activeRoutineId) taskSel.value = String(activeRoutineId);
    }
}

function renderCategoryOptions() {
    const sel = document.getElementById('taskCat');
    if (!sel) return;
    sel.innerHTML = manageCategories.map(c =>
        '<option value="' + c.code + '">' + (c.emoji || '') + ' ' + App.escape(c.name) + '</option>').join('');
}

/* 드롭다운에서 루틴 변경 (tab_management.jsp 의 onchange) */
function changeActiveRoutine(id) {
    activeRoutineId = id ? Number(id) : null;
    openTaskForm();
    renderManage();
}

/* 운영 기간 + 캘린더 + 타임테이블 */
function renderManage() {
    const r = activeRoutine();
    const period = document.getElementById('routinePeriodLabel');
    if (period) {
        period.textContent = r
            ? r.startDate.replaceAll('-', '.') + ' ~ ' + (r.endDate ? r.endDate.replaceAll('-', '.') : '종료일 없음')
            : '-';
    }
    renderCalendar();
    fetchTimeline();
}

/* ---------------- 캘린더 + 요약 카드 ---------------- */
function calendarRange() {
    const c = new Date(calendarCursor);
    if (calendarView === 'week') {
        const from = new Date(c); from.setDate(c.getDate() - c.getDay());
        const to = new Date(from); to.setDate(from.getDate() + 6);
        return { from: from, to: to, count: 7 };
    }
    const first = new Date(c.getFullYear(), c.getMonth(), 1);
    const from = new Date(first); from.setDate(1 - first.getDay());
    const to = new Date(from); to.setDate(from.getDate() + 41);
    return { from: from, to: to, count: 42 };
}

function renderCalendar() {
    const grid = document.getElementById('calendarGrid'), title = document.getElementById('calendarTitle');
    if (!grid || !title) return;
    const range = calendarRange(), c = new Date(calendarCursor);
    title.textContent = calendarView === 'week'
        ? range.from.getFullYear() + '년 ' + (range.from.getMonth() + 1) + '월 ' + range.from.getDate() + '일 ~ '
          + (range.to.getMonth() + 1) + '월 ' + range.to.getDate() + '일'
        : c.getFullYear() + '년 ' + (c.getMonth() + 1) + '월';

    const draw = (stats) => {
        let html = '';
        for (let i = 0; i < range.count; i++) {
            const d = new Date(range.from); d.setDate(range.from.getDate() + i);
            const muted = calendarView === 'month' && d.getMonth() !== c.getMonth();
            html += calendarDayBox(d, muted, stats[App.dateKey(d)]);
        }
        grid.innerHTML = html;
    };
    draw({});
    if (!activeRoutineId) { setSummary(0, 0, null); return; }

    api('GET', MY_API + '/' + activeRoutineId + '/calendar?from=' + App.dateKey(range.from) + '&to=' + App.dateKey(range.to))
        .then(json => {
            if (!json.success) return fail(json, '캘린더를 불러오지 못했습니다.');
            const stats = {};
            json.data.days.forEach(d => { stats[d.date] = d; });
            draw(stats);
            setSummary(json.data.achievementRate, json.data.streakDays, json.data.totalXp);
        });
}

function setSummary(rate, streak, xp) {
    document.getElementById('progressRatio').textContent = rate + '%';
    document.getElementById('userStreak').textContent = String(streak);
    if (xp != null) document.getElementById('totalXp').textContent = xp.toLocaleString() + ' XP';
}

/* 날짜 칸: 완료 ●, 남은 Task ○ (최대 5개) */
function calendarDayBox(d, muted, stat) {
    const ds = App.dateKey(d);
    let dots = '';
    if (stat && stat.planned) {
        const done = Math.min(stat.done, 5), left = Math.min(stat.planned, 5) - done;
        dots = '<div style="font-size: 8px; letter-spacing: 2px; margin-top: 4px;">' + '●'.repeat(done) + '○'.repeat(Math.max(left, 0)) + '</div>';
    }
    return '<div class="cal-day-box ' + (muted ? 'muted' : '') + ' ' + (ds === selectedDate ? 'selected' : '') + '" onclick="App.routine.selectCalendarDate(\'' + ds + '\')">' +
           '<div class="cal-day-name">' + ['일','월','화','수','목','금','토'][d.getDay()] + '</div>' +
           '<div class="cal-date-num">' + d.getDate() + '</div>' + dots +
           '</div>';
}

function selectCalendarDate(ds) { selectedDate = ds; calendarCursor = new Date(ds + 'T00:00:00'); renderManage(); }
function moveCalendar(n) { if (calendarView === 'week') calendarCursor.setDate(calendarCursor.getDate() + n * 7); else { calendarCursor.setDate(1); calendarCursor.setMonth(calendarCursor.getMonth() + n); } renderCalendar(); }
function toggleCalendarView() { calendarView = calendarView === 'week' ? 'month' : 'week'; document.getElementById('calendarViewBtn').textContent = calendarView === 'week' ? '주' : '월'; renderCalendar(); }
function calendarToday() { selectedDate = App.dateKey(); calendarCursor = new Date(selectedDate + 'T00:00:00'); renderManage(); }

/* ---------------- 타임테이블 ---------------- */
function fetchTimeline() {
    if (!activeRoutineId) { renderTimetable(null); return; }
    api('GET', MY_API + '/' + activeRoutineId + '/timeline?date=' + selectedDate)
        .then(json => json.success ? renderTimetable(json.data) : fail(json, '타임테이블을 불러오지 못했습니다.'));
}

function renderTimetable(timeline) {
    const box = document.getElementById('manageTimetable');
    if (!box) return;
    document.getElementById('selectedDateLabel').textContent = selectedDate.replaceAll('-', '.') + ' 기준 · Task를 클릭하면 상세 설정이 열립니다.';
    timelineTasks = timeline ? timeline.tasks : [];

    if (!activeRoutineId) {
        box.innerHTML = emptyBox('아직 루틴이 없습니다.<br>＋ 새 루틴을 만들거나 탐색 탭에서 루틴을 담아보세요!');
        return;
    }
    if (!timelineTasks.length) {
        box.innerHTML = emptyBox('이 날짜에 할 Task가 없습니다.<br>우측에서 Task를 추가해 보세요!');
        return;
    }
    box.innerHTML = timelineTasks.map(function(t, i) {
        const sub = (t.categoryEmoji || '') + ' ' + (t.categoryName || '') + ' · ' + t.repeatLabel
                  + (t.place ? ' · 📍 ' + t.place.name + ' (' + t.allowedRadiusMeters + 'm 인증)' : '');
        // 완료한 Task 는 체크 해제 불가 → 버튼 동작 없음
        const btnClick = t.done ? 'event.stopPropagation()' : 'event.stopPropagation();App.routine.toggleServerDone(' + t.id + ')';
        return '<div class="tt-row ' + (t.done ? 'done' : '') + '" onclick="App.routine.selectServerTask(' + t.id + ')">' +
               '<div class="layout-row align-start justify-center weight-heading color-muted">' + (i + 1) + '</div>' +
               '<div class="weight-heading color-text">' + App.escape(t.time) + '</div>' +
               '<div class="layout-row align-center ds-gap-3 min-width-zero"><span class="color-muted cursor-grab">≡</span><span class="type-xl">' + (t.categoryEmoji || '🌱') + '</span>' +
               '<div class="min-width-zero"><div class="task-title weight-bold">' + App.escape(t.content) + '</div><div class="type-10px color-muted ds-mt-1">' + App.escape(sub) + '</div></div></div>' +
               '<div class="layout-row align-center justify-center"><button type="button" onclick="' + btnClick + '" class="ds-w-9 ds-h-9 radius-pill border-default ' + (t.done ? 'surface-success border-color-success color-white' : 'surface-white border-color-muted color-muted') + '">' + (t.done ? '✓' : '○') + '</button></div>' +
               '</div>';
    }).join('');
}

/* 완료 체크: 팀원 API (POST /user-routines/tasks/complete, GPS 인증 + XP 지급) */
function toggleServerDone(taskId) {
    toast('📍 실천 위치를 확인하고 있습니다…', '🧭');
    if (navigator.geolocation) {
        navigator.geolocation.getCurrentPosition(
            pos => sendCompleteRoutineTask(taskId, pos.coords.latitude, pos.coords.longitude),
            () => sendCompleteRoutineTask(taskId, null, null),
            { enableHighAccuracy: true, timeout: 6000, maximumAge: 0 }
        );
    } else {
        sendCompleteRoutineTask(taskId, null, null);
    }
}

function sendCompleteRoutineTask(taskId, lat, lng) {
    const params = new URLSearchParams({ taskId: String(taskId) });
    if (lat != null) params.append('latitude', String(lat));
    if (lng != null) params.append('longitude', String(lng));
    api('POST', contextPath + '/user-routines/tasks/complete', params)
        .then(json => {
            if (!json.success) return fail(json, '인증 범위를 벗어났거나 완료 처리에 실패했습니다.');
            const data = json.data || {};
            const reward = data.reward || data.rewardResult;
            const earned = data.earnedXp || 0;
            if (reward && reward.isLevelUp && reward.currentLevel) {
                toast('🎉 레벨업! [Lv.' + reward.currentLevel.level + ' ' + reward.currentLevel.title + '] 달성! (+' + earned + ' XP)', '🏆');
            } else {
                toast('🎉 루틴 실천 완료! +' + earned + ' XP');
            }
            renderManage();   // 타임테이블·캘린더·요약 카드(XP, 연속 실천일) 다시 조회
        })
        .catch(() => toast('루틴 완료 통신 중 오류가 발생했습니다.', '⚠️'));
}

/* ---------------- Task 설정 폼 ---------------- */
function selectServerTask(taskId) {
    const t = timelineTasks.find(x => x.id === taskId);
    if (!t) return;
    setVal('editingTaskId', String(t.id));
    setVal('taskRoutine', String(activeRoutineId));
    document.getElementById('taskRoutine').disabled = true;   // 다른 루틴으로 옮기기는 지원하지 않음
    setVal('taskTitle', t.content);
    setVal('taskTime', t.time);
    setVal('taskAlarm', t.alertTime || '');
    if (t.categoryCode) setVal('taskCat', t.categoryCode);
    setVal('taskRepeat', t.repeatType);
    onRepeatChange(t.repeatType);
    document.querySelectorAll('input[name="taskDays"]').forEach(cb => { cb.checked = (t.daysOfWeek || []).includes(cb.value); });
    setVal('taskOnceDate', t.onceDate || '');
    setVal('taskMemo', t.memo || '');
    if (t.place) setTaskPlace(t.place.id, t.place.name, t.allowedRadiusMeters); else clearTaskPlace();
}

/* [＋ 추가] 또는 저장 후: 빈 폼 */
function openTaskForm() {
    const form = document.getElementById('taskForm');
    if (!form) return;
    form.reset();
    setVal('editingTaskId', 'NEW');
    setVal('taskTime', '07:00');
    setVal('taskAlarm', '');
    const routineSel = document.getElementById('taskRoutine');
    if (routineSel) {
        routineSel.disabled = false;
        if (activeRoutineId) routineSel.value = String(activeRoutineId);
    }
    onRepeatChange('DAILY');
    clearTaskPlace();
}

/* 반복: 사용자 지정이면 요일, 1회성이면 날짜 입력 표시 */
function onRepeatChange(type) {
    const days = document.getElementById('repeatDays'), once = document.getElementById('taskOnceDate');
    if (days) days.classList.toggle('is-hidden', type !== 'CUSTOM');
    if (once) {
        once.classList.toggle('is-hidden', type !== 'ONCE');
        if (type === 'ONCE' && !once.value) once.value = selectedDate;
    }
}

function saveTask(e) {
    e.preventDefault();
    const routineId = document.getElementById('taskRoutine').value;
    if (!routineId) return toast('먼저 ＋ 새 루틴으로 루틴을 만들어 주세요.', '⚠️');
    const taskId = document.getElementById('editingTaskId').value;
    const isNew = !taskId || taskId === 'NEW';

    const p = new URLSearchParams();
    p.append('content', document.getElementById('taskTitle').value.trim());
    p.append('time', document.getElementById('taskTime').value);
    p.append('category', document.getElementById('taskCat').value);
    p.append('repeatType', document.getElementById('taskRepeat').value);
    document.querySelectorAll('input[name="taskDays"]:checked').forEach(cb => p.append('daysOfWeek', cb.value));
    p.append('onceDate', document.getElementById('taskOnceDate').value);
    p.append('alertTime', document.getElementById('taskAlarm').value);
    p.append('memo', document.getElementById('taskMemo').value);
    const placeId = document.getElementById('taskPlaceId').value;
    if (placeId) {
        p.append('placeId', placeId);
        p.append('allowedRadiusMeters', document.getElementById('taskRadius').value);
    }

    const url = MY_API + '/' + routineId + '/tasks' + (isNew ? '' : '/' + taskId) + '?date=' + selectedDate;
    api('POST', url, p)
        .then(json => {
            if (!json.success) return fail(json, 'Task 저장에 실패했습니다.');
            toast(isNew ? 'Task를 추가했습니다.' : 'Task를 수정했습니다.');
            if (Number(routineId) !== activeRoutineId) {   // 다른 루틴에 추가했으면 그 루틴으로 전환
                activeRoutineId = Number(routineId);
                renderRoutineSelects();
            }
            openTaskForm();
            renderManage();
        })
        .catch(() => toast('Task 저장 중 통신 오류가 발생했습니다.', '⚠️'));
}

function deleteActiveTask() {
    const taskId = document.getElementById('editingTaskId').value;
    if (!taskId || taskId === 'NEW') return toast('삭제할 Task를 타임테이블에서 먼저 선택해 주세요.');
    if (!confirm('이 Task를 삭제할까요? 완료 기록은 남습니다.')) return;
    api('DELETE', MY_API + '/' + activeRoutineId + '/tasks/' + taskId + '?date=' + selectedDate)
        .then(json => {
            if (!json.success) return fail(json, 'Task 삭제에 실패했습니다.');
            toast('Task를 삭제했습니다.');
            openTaskForm();
            renderManage();
        });
}

/* ---------------- Task 장소: 카카오 검색 → 선택 → places 저장 → placeId ---------------- */
function searchTaskPlace() {
    const q = document.getElementById('taskPlaceSearch').value.trim();
    if (!q) return toast('검색할 장소 이름을 입력해 주세요.');
    if (typeof MapUtil === 'undefined' || !MapUtil.searchPlaces) {
        return toast('장소 검색을 사용할 수 없습니다. (카카오 지도 SDK 확인 필요)', '⚠️');
    }
    MapUtil.searchPlaces({ query: q, lat: userLocation.lat, lng: userLocation.lng, radius: 20000 })
        .then(res => {
            taskPlaceResults = (res && res.success && Array.isArray(res.data)) ? res.data.slice(0, 8) : [];
            const box = document.getElementById('taskPlaceResults');
            box.classList.remove('is-hidden');
            box.innerHTML = taskPlaceResults.length
                ? taskPlaceResults.map((pl, i) =>
                    '<div style="cursor: pointer; padding: 8px 4px; border-bottom: 1px solid #eee;" onclick="App.routine.pickTaskPlace(' + i + ')">' +
                    '<div class="weight-bold type-sm">' + App.escape(pl.name || '') + '</div>' +
                    '<div class="type-xs color-muted">' + App.escape(pl.roadAddress || pl.address || '') + '</div></div>').join('')
                : '<div class="type-xs color-muted" style="padding: 8px 4px;">검색 결과가 없습니다.</div>';
        })
        .catch(() => toast('장소 검색 중 오류가 발생했습니다.', '⚠️'));
}

function pickTaskPlace(i) {
    const pl = taskPlaceResults[i];
    if (!pl) return;
    const body = new URLSearchParams({
        kakaoPlaceId: String(pl.kakaoPlaceId || pl.id || ''),
        name: pl.name || '',
        latitude: String(pl.latitude),
        longitude: String(pl.longitude),
        roadAddress: pl.roadAddress || '',
        addressName: pl.address || pl.addressName || ''
    });
    api('POST', contextPath + '/api/routines/places', body)
        .then(json => {
            if (!json.success) return fail(json, '장소를 저장하지 못했습니다.');
            setTaskPlace(json.data.placeId, pl.name, 50);
            document.getElementById('taskPlaceResults').classList.add('is-hidden');
        });
}

function setTaskPlace(id, name, radius) {
    setVal('taskPlaceId', String(id));
    setVal('taskPlaceSearch', '');
    document.getElementById('taskPlaceName').textContent = '📍 ' + name;
    document.getElementById('taskPlaceSelected').classList.remove('is-hidden');
    document.getElementById('radiusControl').classList.remove('is-hidden');
    setVal('taskRadius', String(radius || 50));
    document.getElementById('radiusValueLabel').textContent = (radius || 50) + 'm';
}

function clearTaskPlace() {
    setVal('taskPlaceId', '');
    const sel = document.getElementById('taskPlaceSelected'), results = document.getElementById('taskPlaceResults'), radius = document.getElementById('radiusControl');
    if (sel) sel.classList.add('is-hidden');
    if (results) results.classList.add('is-hidden');
    if (radius) radius.classList.add('is-hidden');
}

/* ---------------- 새 루틴 / 기본 정보 모달 (modal/routineFormModal.jsp) ---------------- */
function fillRoutineForm(r) {
    setVal('rfRoutineId', r ? String(r.id) : '');
    setVal('rfName', r ? r.title : '');
    setVal('rfStart', r ? r.startDate : App.dateKey());
    setVal('rfEnd', r && r.endDate ? r.endDate : '');
    setVal('rfDesc', r && r.description ? r.description : '');
    setVal('rfTags', r ? r.tags.join(', ') : '');
    const visibility = r ? r.visibility : 'PRIVATE';
    const adopted = !!(r && r.adopted);
    document.querySelectorAll('input[name="rfVisibility"]').forEach(x => { x.checked = x.value === visibility; x.disabled = adopted; });
    document.getElementById('rfAdoptedNote').classList.toggle('is-hidden', !adopted);
    const picked = r ? r.categories.map(c => c.code) : [];
    document.getElementById('rfCategories').innerHTML = manageCategories.map(c =>
        '<label><input type="checkbox" name="rfCategory" value="' + c.code + '"' + (picked.includes(c.code) ? ' checked' : '') + '> '
        + (c.emoji || '') + ' ' + App.escape(c.name) + '</label>').join('');
    document.getElementById('rfDeleteBtn').classList.toggle('is-hidden', !r);
    togglePublicFields();
}

/* 공개를 고르면 설명·카테고리·태그 입력 표시 */
function togglePublicFields() {
    const checked = document.querySelector('input[name="rfVisibility"]:checked');
    document.getElementById('rfPublicFields').classList.toggle('is-hidden', !(checked && checked.value === 'PUBLIC'));
}

function openRoutineCreateModal() {
    document.getElementById('rfTitle').textContent = '새 루틴 만들기';
    fillRoutineForm(null);
    document.getElementById('routineFormModal').classList.add('show');
}

function openRoutineInfo() {
    const r = activeRoutine();
    if (!r) return toast('먼저 ＋ 새 루틴으로 루틴을 만들어 주세요.');
    document.getElementById('rfTitle').textContent = '루틴 기본 정보';
    fillRoutineForm(r);
    document.getElementById('routineFormModal').classList.add('show');
}

function saveRoutineInfo(e) {
    e.preventDefault();
    const id = document.getElementById('rfRoutineId').value;
    const visibility = document.querySelector('input[name="rfVisibility"]:checked');
    const p = new URLSearchParams();
    p.append('title', document.getElementById('rfName').value.trim());
    p.append('startDate', document.getElementById('rfStart').value);
    p.append('endDate', document.getElementById('rfEnd').value);
    p.append('visibility', visibility ? visibility.value : 'PRIVATE');
    p.append('description', document.getElementById('rfDesc').value);
    document.querySelectorAll('input[name="rfCategory"]:checked').forEach(x => p.append('categories', x.value));
    document.getElementById('rfTags').value.split(',').map(s => s.trim()).filter(Boolean).forEach(t => p.append('tags', t));

    api('POST', MY_API + (id ? '/' + id : ''), p)
        .then(json => {
            if (!json.success) return fail(json, '루틴 저장에 실패했습니다.');
            toast(id ? '루틴 정보를 저장했습니다.' : '새 루틴을 만들었습니다!');
            hide('routineFormModal');
            loadManage(json.data.id);
        })
        .catch(() => toast('루틴 저장 중 오류가 발생했습니다.', '⚠️'));
}

function deleteRoutine() {
    const id = document.getElementById('rfRoutineId').value;
    if (!id) return;
    if (!confirm('이 루틴을 삭제할까요? 완료 기록과 XP는 남습니다.')) return;
    api('DELETE', MY_API + '/' + id)
        .then(json => {
            if (!json.success) return fail(json, '루틴 삭제에 실패했습니다.');
            toast('루틴을 삭제했습니다.');
            hide('routineFormModal');
            activeRoutineId = null;
            loadManage();
        });
}

    function initRoutineMap() {
        if (typeof MapUtil === 'undefined' || typeof kakao === 'undefined' || !kakao.maps) {
            return;
        }
        const container = document.getElementById('customMapContainer');
        if (!container) return;

        routineMap = MapUtil.init('customMapContainer', {
            lat: userLocation.lat,
            lng: userLocation.lng,
            level: 4,
            showControls: true
        });

        searchNearbyRoutinePlaces();
    }

    const CATEGORY_QUERY_MAP = {
        '전체': ['공원', '피트니스', '도서관'],
        '운동': ['헬스장', '피트니스', '클라이밍', '필라테스'],
        '미라클 모닝': ['공원', '산책로', '러닝'],
        '공부': ['도서관', '스터디카페', '북카페'],
        '건강': ['공원', '산책로', '둘레길'],
        '숙면': ['공원', '산책로'],
        '직장인': ['피트니스', '카페', '도서관']
    };

    const NOISE_BLACKLIST = [
        '새마을', '재활', '본부', '척추', '통증', '요양', '병원', '의원', '한의원', '치과',
        '정형외과', '내과', '외과', '클리닉', '협의회', '지회', '지부', '사무소', '협회', '연맹',
        '재단', '위원회', '공단', '공사', '판매', '도소매', '상사', '용품', '유통', '기구',
        '교정', '치료', '보호', '복지', '장례', '관리실', '학회', '연구소', '정비'
    ];

    function isNoisePlace(place) {
        if (!place) return true;
        const targetText = ((place.name || '') + ' ' + (place.categoryName || '') + ' ' + (place.originalCategoryName || '') + ' ' + (place.roadAddress || '') + ' ' + (place.address || '')).toLowerCase();
        return NOISE_BLACKLIST.some(kw => targetText.includes(kw));
    }

    async function searchNearbyRoutinePlaces(keyword) {
        if (!routineMap || typeof MapUtil === 'undefined') return;

        const lat = userLocation.lat;
        const lng = userLocation.lng;
        const searchRadius = Math.max(radiusKm * 1000, 4000);
        const ctx = (document.body && document.body.dataset && document.body.dataset.contextPath) || '';

        let combinedPlaces = [];

        // 1단계: DB 등록 검증 루틴 스팟 우선 조회 (/api/places/nearby)
        try {
            const dbRes = await fetch(`${ctx}/api/places/nearby?lat=${lat}&lng=${lng}&radius=${searchRadius}`);
            if (dbRes.ok) {
                const dbJson = await dbRes.json();
                if (dbJson && dbJson.success && Array.isArray(dbJson.data)) {
                    combinedPlaces = dbJson.data
                        .filter(p => !isNoisePlace(p))
                        .map(p => ({
                            ...p,
                            isDbSpot: true,
                            themeName: p.themeName || '#인증스팟'
                        }));
                }
            }
        } catch (e) {
            console.warn('[Routine] DB nearby places lookup failed, fallback to Kakao API:', e);
        }

        // 2단계: DB 장소가 부족할 경우 정제된 카카오 API 검색으로 폴백 채우기
        if (combinedPlaces.length < 6) {
            const queries = keyword ? [keyword] : (CATEGORY_QUERY_MAP[routineCategory] || ['공원', '피트니스']);
            const existingNames = new Set(combinedPlaces.map(p => (p.name || '').trim()));

            for (const query of queries) {
                if (combinedPlaces.length >= 6) break;
                try {
                    const kakaoRes = await MapUtil.searchPlaces({
                        query: query,
                        lat: lat,
                        lng: lng,
                        radius: searchRadius
                    });

                    if (kakaoRes && kakaoRes.success && Array.isArray(kakaoRes.data)) {
                        for (const place of kakaoRes.data) {
                            const trimmedName = (place.name || '').trim();
                            if (!existingNames.has(trimmedName) && !isNoisePlace(place)) {
                                existingNames.add(trimmedName);
                                combinedPlaces.push({
                                    ...place,
                                    isDbSpot: false
                                });
                                if (combinedPlaces.length >= 6) break;
                            }
                        }
                    }
                } catch (err) {
                    console.warn('[Routine] Kakao place fallback query failed:', query, err);
                }
            }
        }

        nearbyRoutinePlaces = combinedPlaces;
        renderNearbyPlaces();
    }

    function renderNearbyPlaces() {
        if (!routineMap || typeof MapUtil === 'undefined') return;

        MapUtil.renderPlaceMarkers(routineMap, nearbyRoutinePlaces, {
            clearPrevious: true,
            onSelect: (place) => {
                selectRoutinePlace(place);
            }
        });

        const container = document.getElementById('nearbyPlaces');
        if (!container) return;

        if (!nearbyRoutinePlaces.length) {
            container.innerHTML = '<div class="empty span-full" style="padding: 20px; text-align: center; color: var(--color-muted);">주변에 검색된 루틴 스팟이 없습니다.</div>';
            return;
        }

        container.innerHTML = nearbyRoutinePlaces.slice(0, 6).map(place => {
            const distText = place.distanceInMeters
                ? (place.distanceInMeters < 1000 ? place.distanceInMeters + 'm' : (place.distanceInMeters / 1000).toFixed(1) + 'km')
                : '';
            const badgeLabel = place.isDbSpot ? '⭐ ' + (place.themeName || '#인증스팟') : (place.themeName || '#루틴스팟');
            return `
                <div class="card surface-white border-default border-color-border radius-md ds-p-4 cursor-pointer" onclick="App.routine.focusPlace(${place.latitude}, ${place.longitude})">
                    <div class="layout-row justify-between align-center">
                        <span class="pill" style="font-size: 11px;">${App.escape(badgeLabel)}</span>
                        <span class="type-xs color-muted">${distText ? '📍 ' + distText : ''}</span>
                    </div>
                    <h4 class="weight-bold type-sm ds-mt-2">${App.escape(place.name)}</h4>
                    <p class="type-xs color-muted ds-mt-1">${App.escape(place.roadAddress || place.address || '')}</p>
                    <div class="layout-row ds-gap-2 ds-mt-3">
                        <button type="button" class="btn btn-secondary ds-px-2 ds-py-1 type-xs" onclick="event.stopPropagation(); App.routine.selectRoutinePlaceForTask('${App.escape(place.name)}')">이 장소로 루틴 만들기</button>
                    </div>
                </div>
            `;
        }).join('');
    }

    function focusPlace(lat, lng) {
        if (routineMap && typeof MapUtil !== 'undefined') {
            MapUtil.moveTo(routineMap, lat, lng);
        }
    }

    function selectRoutinePlace(place) {
        showPlaceDetail(place);
        if (routineMap && place.latitude && place.longitude) {
            MapUtil.moveTo(routineMap, place.latitude, place.longitude);
        }
    }

       /* "이 장소로 루틴 만들기": 관리 탭 Task 폼에서 그 이름으로 장소 검색 */
    function selectRoutinePlaceForTask(placeName) {
        switchTab('manage');
        openTaskForm();
        setVal('taskPlaceSearch', placeName);
        searchTaskPlace();
    }


// 주변 장소 필터: 기존 지도 검색 로직 재사용
function setRadius(km, button) {
    radiusKm = Math.max(1, Math.min(10, Number(km) || 3));
    document.querySelectorAll('#filterModal [data-radius]').forEach(el => {
        el.classList.toggle('active', el === button);
    });
}
function applyPlaceFilters() {
    hide('filterModal');
    searchNearbyRoutinePlaces();
    toast('주변 장소 필터가 적용되었습니다.', '📍');
}
function showPlaceDetail(place) {
    if (!place) return;
    const fields = {
        pmType: place.themeName || '루틴 스팟',
        pmName: place.name || '',
        pmAddress: place.roadAddress || place.address || '',
        pmDistance: place.distanceInMeters != null ? place.distanceInMeters + 'm' : '-',
        pmRoutineCount: place.routineCount != null ? String(place.routineCount) : '-',
        pmHot: place.hotScore != null ? String(place.hotScore) : '-'
    };
    Object.entries(fields).forEach(([id, value]) => {
        const el = document.getElementById(id);
        if (el) el.textContent = value;
    });
    const list = document.getElementById('pmRoutines');
    if (list) list.textContent = '등록된 장소별 루틴 정보가 없습니다.';
    document.getElementById('placeModal')?.classList.add('show');
}

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') ['routineModal','placeModal','filterModal','routineFormModal'].forEach(hide);
    });

    document.addEventListener('DOMContentLoaded', () => {
        initRoutineMap();
        if (location.hash === '#manage') switchTab('manage');
    });

        App.routine = {
        setRadius, applyPlaceFilters, showPlaceDetail, toast, hide, closeModal, switchTab, filteredRoutines, routineCard, renderExplore,
        clearSearch, setCategory, openRoutine, toggleModalTasks, adoptRoutineToServer,
        addAllRoutineTasks, addSelectedRoutineTasks, locateUser, focusUser, openPlaceFilters,
        loadManage, changeActiveRoutine, renderManage, renderCalendar, selectCalendarDate, moveCalendar,
        toggleCalendarView, calendarToday, toggleServerDone, selectServerTask, openTaskForm, onRepeatChange,
        saveTask, deleteActiveTask, searchTaskPlace, pickTaskPlace, clearTaskPlace,
        openRoutineCreateModal, openRoutineInfo, saveRoutineInfo, togglePublicFields, deleteRoutine,
        initRoutineMap, searchNearbyRoutinePlaces, renderNearbyPlaces, focusPlace, selectRoutinePlace, selectRoutinePlaceForTask
    };
})();
