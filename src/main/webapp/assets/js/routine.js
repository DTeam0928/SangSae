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

/* 2. 서버 타임테이블 조회 (DB 조회만 허용) */
function fetchTimelineFromServer() {
    fetch(contextPath + '/user-routines?date=' + selectedDate)
        .then(res => res.ok ? res.json() : null)
        .then(json => {
            if (json && json.success && Array.isArray(json.data)) {
                serverTimelineTasks = json.data;
            } else {
                serverTimelineTasks = [];
            }
            renderTimetableWithServer(serverTimelineTasks);
        })
        .catch(() => {
            serverTimelineTasks = [];
            renderTimetableWithServer([]);
        });
}

document.addEventListener('DOMContentLoaded', function() {
    fetchRoutinesFromServer();
    renderManage();
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
            activeUserRoutineServerId = json.data && json.data.id;
            toast('루틴을 내 일상에 추가했습니다! (Fork 완료)', '✨');
            hide('routineModal');
            switchTab('manage');
            fetchTimelineFromServer();
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

function renderManage() {
    const sel = document.getElementById('activeRoutineSelect');
    if (sel) {
        sel.innerHTML = routines.length
            ? routines.map(x => '<option value="' + x.id + '">' + App.escape(x.name) + '</option>').join('')
            : '<option value="default">내 활성 루틴</option>';
    }
    const period = document.getElementById('routinePeriodLabel');
    if (period) period.textContent = selectedDate.replaceAll('-', '.') + ' 기준';
    renderCalendar();
    fetchTimelineFromServer();
}

function renderCalendar() {
    const grid = document.getElementById('calendarGrid'), title = document.getElementById('calendarTitle'), c = new Date(calendarCursor);
    if (!grid || !title) return;
    if (calendarView === 'week') {
        const day = c.getDay(), sun = new Date(c);
        sun.setDate(c.getDate() - day);
        const endDay = new Date(sun.getFullYear(), sun.getMonth(), sun.getDate() + 6);
        title.textContent = sun.getFullYear() + '년 ' + (sun.getMonth() + 1) + '월 ' + sun.getDate() + '일 ~ ' + (endDay.getMonth() + 1) + '월 ' + endDay.getDate() + '일';
        let html = '';
        for (let i = 0; i < 7; i++) { const d = new Date(sun); d.setDate(sun.getDate() + i); html += calendarDayBox(d, false); }
        grid.innerHTML = html;
    } else {
        title.textContent = c.getFullYear() + '년 ' + (c.getMonth() + 1) + '월';
        const first = new Date(c.getFullYear(), c.getMonth(), 1), start = new Date(first);
        start.setDate(1 - first.getDay());
        let html = '';
        for (let i = 0; i < 42; i++) { const d = new Date(start); d.setDate(start.getDate() + i); html += calendarDayBox(d, d.getMonth() !== c.getMonth()); }
        grid.innerHTML = html;
    }
}

function calendarDayBox(d, muted) {
    const ds = App.dateKey(d);
    return '<div class="cal-day-box ' + (muted ? 'muted' : '') + ' ' + (ds === selectedDate ? 'selected' : '') + '" onclick="App.routine.selectCalendarDate(\'' + ds + '\')">' +
           '<div class="cal-day-name">' + ['일','월','화','수','목','금','토'][d.getDay()] + '</div>' +
           '<div class="cal-date-num">' + d.getDate() + '</div>' +
           '</div>';
}

function selectCalendarDate(ds) { selectedDate = ds; calendarCursor = new Date(ds + 'T00:00:00'); renderManage(); }
function moveCalendar(n) { if (calendarView === 'week') calendarCursor.setDate(calendarCursor.getDate() + n * 7); else { calendarCursor.setDate(1); calendarCursor.setMonth(calendarCursor.getMonth() + n); } renderCalendar(); }
function toggleCalendarView() { calendarView = calendarView === 'week' ? 'month' : 'week'; document.getElementById('calendarViewBtn').textContent = calendarView === 'week' ? '주' : '월'; renderCalendar(); }
function calendarToday() { selectedDate = App.dateKey(); calendarCursor = new Date(selectedDate + 'T00:00:00'); renderManage(); }

/* 서버 기반 타임테이블 렌더링 (순수 DB 데이터만 출력) */
function renderTimetableWithServer(serverTasks) {
    const box = document.getElementById('manageTimetable');
    if (!box) return;

    document.getElementById('selectedDateLabel').textContent = selectedDate.replaceAll('-', '.') + ' 기준 타임테이블';

    if (!serverTasks.length) {
        box.innerHTML = '<div class="empty" style="padding: 30px; text-align: center; color: var(--color-muted);">등록된 루틴 Task가 없습니다.<br>우측에서 Task를 추가하거나 탐색 탭에서 루틴을 담아보세요!</div>';
        document.getElementById('progressRatio').textContent = '0%';
        return;
    }

    box.innerHTML = serverTasks.map(function(item, i) {
        const isDone = item.completed;
        const icon = '🌱';
        const radiusText = (item.allowedRadiusMeters && item.allowedRadiusMeters > 0) ? (' · 📍 ' + item.allowedRadiusMeters + 'm 반경 인증') : '';
        const subInfo = (item.category || '일상') + ' · ' + (item.repeatType || '매일') + radiusText + (item.placeName ? ' · ' + item.placeName : '');

        return '<div class="tt-row ' + (isDone ? 'done' : '') + '" onclick="App.routine.selectServerTask(' + item.taskId + ')">' +
               '<div class="layout-row align-start justify-center weight-heading color-muted">' + (i + 1) + '</div>' +
               '<div class="weight-heading color-text">' + App.escape(item.targetTime ? item.targetTime.slice(0, 5) : '07:00') + '</div>' +
               '<div class="layout-row align-center ds-gap-3 min-width-zero"><span class="color-muted cursor-grab">≡</span><span class="type-xl">' + icon + '</span>' +
               '<div class="min-width-zero"><div class="task-title weight-bold">' + App.escape(item.content) + '</div><div class="type-10px color-muted ds-mt-1">' + App.escape(subInfo) + '</div></div></div>' +
               '<div class="layout-row align-center justify-center"><button type="button" onclick="event.stopPropagation();App.routine.toggleServerDone(' + item.taskId + ')" class="ds-w-9 ds-h-9 radius-pill border-default ' + (isDone ? 'surface-success border-color-success color-white' : 'surface-white border-color-muted color-muted') + '">' + (isDone ? '✓' : '○') + '</button></div>' +
               '</div>';
    }).join('');

    const all = serverTasks.length, done = serverTasks.filter(x => x.completed).length;
    document.getElementById('progressRatio').textContent = all ? Math.round(done / all * 100) + '%' : '0%';
}

/* 루틴 완료 및 가변 반경(50m~1km) GPS 인증 */
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

    fetch(contextPath + '/user-routines/tasks/complete', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(res => res.json())
    .then(json => {
        if (json && json.success) {
            const resData = json.data;
            const earned = resData.earnedXp || 100;
            const streak = resData.streakCount || 1;
            totalXp += earned;
            userStreak = streak;

            const xpElem = document.getElementById('totalXp');
            if (xpElem) xpElem.textContent = totalXp.toLocaleString() + ' XP';
            const streakElem = document.getElementById('userStreak');
            if (streakElem) streakElem.textContent = String(userStreak);

            if (resData.reward && resData.reward.isLevelUp) {
                toast(`🎉 레벨업! [Lv.${resData.reward.currentLevel.level} ${resData.reward.currentLevel.title}] 달성! (+${earned} XP)`, '🏆');
            } else {
                toast(`🎉 루틴 실천 완료! +${earned} XP (연속 ${streak}일 달성!)`);
            }
            fetchTimelineFromServer();
        } else {
            toast(json.message || '인증 범위를 벗어났거나 완료 처리에 실패했습니다.', '⚠️');
        }
    })
    .catch(() => {
        toast('루틴 완료 통신 중 오류가 발생했습니다.', '⚠️');
    });
}

function selectServerTask(taskId) {
    const item = serverTimelineTasks.find(x => x.taskId === taskId);
    if (!item) return;
    document.getElementById('editingTaskId').value = String(taskId);
    document.getElementById('taskTitle').value = item.content || '';
    document.getElementById('taskTime').value = item.targetTime ? item.targetTime.slice(0, 5) : '07:00';
    document.getElementById('taskCat').value = item.category || '일상';
    document.getElementById('taskLocation').value = item.placeName || '';
    const hasRadius = item.allowedRadiusMeters && item.allowedRadiusMeters > 0;
    const chk = document.getElementById('taskLocationReq');
    if (chk) chk.checked = hasRadius;
    toggleLocationRadius(hasRadius);
    if (hasRadius) {
        document.getElementById('taskRadius').value = item.allowedRadiusMeters;
        document.getElementById('radiusValueLabel').textContent = item.allowedRadiusMeters + 'm';
    }
    document.getElementById('taskMemo').value = item.memo || '';
}

function openTaskForm() {
    document.getElementById('taskForm').reset();
    document.getElementById('taskTime').value = '07:00';
    document.getElementById('taskAlarm').value = '07:00';
    document.getElementById('editingTaskId').value = 'NEW';
    const chk = document.getElementById('taskLocationReq');
    if (chk) chk.checked = false;
    toggleLocationRadius(false);
}

/* 세부 Task 저장: 오직 백엔드 DB 통신만 수행 (POST /user-routines/tasks) */
function saveTask(e) {
    e.preventDefault();
    const title = document.getElementById('taskTitle').value.trim();
    const time = document.getElementById('taskTime').value;
    const cat = document.getElementById('taskCat').value;
    const locationReq = document.getElementById('taskLocationReq') ? document.getElementById('taskLocationReq').checked : false;
    const radius = locationReq ? parseInt(document.getElementById('taskRadius').value) : 50;
    const memo = document.getElementById('taskMemo').value;

    if (!title) return;

    const userRoutineId = activeUserRoutineServerId || 1;
    const params = new URLSearchParams({
        userRoutineId: String(userRoutineId),
        content: title,
        targetTime: time + ':00',
        category: cat,
        repeatType: 'DAILY',
        allowedRadiusMeters: String(radius),
        memo: memo
    });

    fetch(contextPath + '/user-routines/tasks', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(res => res.json())
    .then(json => {
        if (json && json.success) {
            toast('새 Task를 DB에 저장했습니다!');
            fetchTimelineFromServer();
            openTaskForm();
        } else {
            toast(json.message || 'Task 저장에 실패했습니다.', '⚠️');
        }
    })
    .catch(() => {
        toast('Task 저장 중 통신 오류가 발생했습니다.', '⚠️');
    });
}

function deleteActiveTask() {
    toast('Task를 선택 후 관리하세요.');
}

function openRoutineCreateModal() {
    document.getElementById('rfTitle').textContent = '새 루틴 만들기';
    document.getElementById('rfName').value = '';
    document.getElementById('rfDesc').value = '';
    document.getElementById('rfStart').value = App.dateKey();
    let d = new Date(); d.setDate(d.getDate() + 30);
    document.getElementById('rfEnd').value = App.dateKey(d);
    document.getElementById('routineFormModal').classList.add('show');
}

function openRoutineInfo() {
    document.getElementById('rfTitle').textContent = '루틴 기본 정보';
    document.getElementById('rfName').value = '내 일상 루틴';
    document.getElementById('rfDesc').value = '';
    document.getElementById('rfStart').value = selectedDate;
    document.getElementById('routineFormModal').classList.add('show');
}

function saveRoutineInfo(e) {
    e.preventDefault();
    const n = document.getElementById('rfName').value.trim(), s = document.getElementById('rfStart').value, en = document.getElementById('rfEnd').value;
    if (en < s) return toast('종료일은 시작일보다 빠를 수 없습니다.');

    const params = new URLSearchParams({ title: n, startDate: s, endDate: en });
    fetch(contextPath + '/user-routines', {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params
    })
    .then(res => res.json())
    .then(json => {
        if (json && json.success) {
            activeUserRoutineServerId = json.data && json.data.id;
            toast('새 루틴을 DB에 생성했습니다!');
            hide('routineFormModal');
            fetchTimelineFromServer();
        } else {
            toast(json.message || '루틴 생성에 실패했습니다.', '⚠️');
        }
    })
    .catch(() => {
        toast('루틴 생성 중 오류가 발생했습니다.', '⚠️');
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

    function selectRoutinePlaceForTask(placeName) {
        switchTab('manage');
        openTaskForm();
        const locInput = document.getElementById('taskLocation');
        if (locInput) locInput.value = placeName;
        const chk = document.getElementById('taskLocationReq');
        if (chk) {
            chk.checked = true;
            toggleLocationRadius(true);
        }
        toast(`'${placeName}' 장소가 Task에 설정되었습니다.`, '📍');
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
        renderManage, renderCalendar, calendarDayBox, selectCalendarDate, moveCalendar,
        toggleCalendarView, calendarToday, renderTimetableWithServer, toggleServerDone,
        selectServerTask, openTaskForm, saveTask, deleteActiveTask, openRoutineCreateModal,
        openRoutineInfo, saveRoutineInfo, toggleLocationRadius,
        initRoutineMap, searchNearbyRoutinePlaces, renderNearbyPlaces, focusPlace, selectRoutinePlace, selectRoutinePlaceForTask
    };
})();
