<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style>
/* ===== 루틴 관리 탭 고유 스타일 ===== */
.tt-head, .tt-row {
    display: grid;
    grid-template-columns: 48px 105px minmax(0,1fr) 68px;
    align-items: stretch;
}
.tt-head {
    background: var(--color-bg);
    color: var(--color-muted);
    font-size: var(--fs-small);
    font-weight: 800;
}
.tt-head > div, .tt-row > div {
    padding: 10px 12px;
    border-right: 1px solid var(--color-border);
}
.tt-head > div:last-child, .tt-row > div:last-child { border-right: 0; }

.tt-row {
    min-height: 76px;
    border-top: 1px solid var(--color-border);
    cursor: pointer;
}
.tt-row:hover { background: var(--color-bg); }
.tt-row.selected {
    background: var(--color-main);
    box-shadow: inset 3px 0 var(--color-dark);
}
.tt-row.done .task-title {
    text-decoration: line-through;
    color: var(--color-muted);
}

.timeline-table {
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    overflow: hidden;
    background: var(--color-white);
}

.cal-grid {
    display: grid;
    grid-template-columns: repeat(7, minmax(0,1fr));
    gap: 6px;
}
.cal-day-box {
    min-height: 84px;
    border: 1px solid var(--color-border);
    border-radius: var(--radius-md);
    background: var(--color-white);
    padding: 9px;
    cursor: pointer;
    position: relative;
}
.cal-day-box:hover { border-color: var(--color-dark); }
.cal-day-box.selected {
    border: 2px solid var(--color-dark);
    background: var(--color-main);
}
.cal-day-box.muted { opacity: .42; }

.cal-day-name { font-size: 10px; font-weight: 800; color: var(--color-muted); }
.cal-date-num { font-size: 18px; font-weight: 900; margin-top: 3px; }
.cal-dots { display: flex; gap: 3px; margin-top: 8px; flex-wrap: wrap; }

.dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-border); }
.dot.active { background: var(--color-dark); }
.dot.done { background: var(--color-success); }

@media(max-width:700px) {
    .tt-head, .tt-row { grid-template-columns: 34px 70px minmax(0,1fr) 50px; }
    .tt-row { min-height: 82px; }
    .tt-row > div, .tt-head > div { padding: 8px 6px; }
    .cal-day-box { min-height: 70px; padding: 7px; }
    .cal-date-num { font-size: 15px; }
}
</style>

<!-- 통계 요약 카드 -->
<div class="grid grid-cols-1 md:grid-cols-3 gap-4">
  <div class="bg-gradient-to-br from-sky-500 to-blue-600 rounded-2xl p-5 text-white">
    <div class="text-sm text-sky-100">선택 기간 달성률</div>
    <div id="progressRatio" class="text-3xl font-black mt-1">0%</div>
  </div>
  <div class="bg-white border border-slate-200 rounded-2xl p-5">
    <div class="text-sm text-slate-500">연속 실천일</div>
    <div class="text-3xl font-black mt-1">12<span class="text-base text-slate-400 ml-1">일</span></div>
  </div>
  <div class="bg-white border border-slate-200 rounded-2xl p-5">
    <div class="text-sm text-slate-500">누적 XP</div>
    <div id="totalXp" class="text-3xl font-black text-sky-500 mt-1">1,240 XP</div>
  </div>
</div>

<!-- 활성 루틴 선택 및 정보 -->
<section class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
  <div class="flex flex-col lg:flex-row gap-4 lg:items-center justify-between">
    <div class="flex flex-col md:flex-row md:items-center gap-3">
      <select id="activeRoutineSelect" onchange="changeActiveRoutine(this.value)" class="bg-slate-50 border border-slate-200 text-lg font-black rounded-xl px-4 py-3 min-w-[260px]"></select>
      <div class="text-xs text-slate-500 bg-slate-50 rounded-xl px-3 py-2">운영 기간 <b id="routinePeriodLabel"></b></div>
    </div>
    <div class="flex gap-2">
      <button type="button" onclick="openRoutineInfo()" class="px-4 py-2 bg-slate-100 text-slate-700 font-bold rounded-xl text-sm">⚙ 기본 정보</button>
      <button type="button" onclick="openRoutineCreateModal()" class="px-4 py-2 bg-sky-50 text-sky-600 font-bold rounded-xl text-sm">＋ 새 루틴</button>
    </div>
  </div>
</section>

<!-- 루틴 캘린더 -->
<section class="bg-white border border-slate-200 rounded-2xl p-4 md:p-5 shadow-sm">
  <div class="flex flex-col md:flex-row md:items-center justify-between gap-3">
    <div>
      <h2 class="font-black text-lg">📅 루틴 캘린더</h2>
      <p class="text-xs text-slate-400 mt-1">주/월 보기로 수행 계획과 완료 상태를 확인하세요.</p>
    </div>
    <div class="flex items-center gap-2">
      <div class="flex gap-1">
        <button type="button" onclick="moveCalendar(-1)" class="w-9 h-9 rounded-lg bg-slate-100 font-black">‹</button>
        <button type="button" onclick="moveCalendar(1)" class="w-9 h-9 rounded-lg bg-slate-100 font-black">›</button>
      </div>
      <button type="button" id="calendarViewBtn" onclick="toggleCalendarView()" class="px-4 py-2 rounded-xl bg-sky-50 text-sky-600 text-xs font-black">주</button>
    </div>
  </div>
  <div class="flex items-center justify-between mt-4 mb-3">
    <div id="calendarTitle" class="font-black text-lg"></div>
    <button type="button" onclick="calendarToday()" class="text-xs font-bold text-sky-600">오늘</button>
  </div>
  <div id="calendarGrid" class="cal-grid"></div>
</section>

<!-- 타임테이블 및 Task 설정 -->
<div class="grid grid-cols-1 lg:grid-cols-12 gap-6 items-start">
  <section class="lg:col-span-8 bg-white border border-slate-200 rounded-2xl p-4 md:p-5 shadow-sm">
    <div class="flex flex-col md:flex-row md:items-center justify-between gap-3 pb-3 border-b border-slate-100">
      <div>
        <h2 class="font-black text-lg">오늘의 루틴 타임테이블</h2>
        <p id="selectedDateLabel" class="text-xs text-slate-400 mt-1"></p>
      </div>
    </div>
    <div class="timeline-table mt-4">
      <div class="tt-head">
        <div>#</div><div>시간</div><div>오늘 해야 할 루틴</div><div>상태</div>
      </div>
      <div id="manageTimetable"></div>
    </div>
  </section>

  <aside class="lg:col-span-4 bg-white border border-slate-200 rounded-2xl p-5 shadow-sm lg:sticky lg:top-24">
    <div class="flex justify-between items-center pb-3 border-b border-slate-100">
      <h2 class="font-black">Task 설정</h2>
      <button type="button" onclick="openTaskForm()" class="px-3 py-2 bg-sky-500 text-white rounded-lg text-xs font-bold">＋ 추가</button>
    </div>
    <form id="taskForm" class="space-y-4 mt-4" onsubmit="saveTask(event)">
      <input type="hidden" id="editingTaskId">
      <div>
        <label class="text-xs font-bold" for="taskTitle">루틴 내용</label>
        <input id="taskTitle" required class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3 text-sm">
      </div>
      <div class="grid grid-cols-2 gap-3">
        <div>
          <label class="text-xs font-bold" for="taskTime">시간</label>
          <input id="taskTime" type="time" required class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3">
        </div>
        <div>
          <label class="text-xs font-bold" for="taskCat">카테고리</label>
          <select id="taskCat" class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-2 py-3 text-xs">
            <option>💪 운동</option><option>💧 건강</option><option>📚 자기계발</option><option>🧠 공부</option><option>💼 업무</option><option>🏠 일상</option>
          </select>
        </div>
      </div>
      <div>
        <label class="text-xs font-bold" for="taskLocation">장소</label>
        <input id="taskLocation" class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3 text-sm" placeholder="예: 집 / 헬스장 / 광안리">
      </div>
      <div>
        <label class="text-xs font-bold" for="taskRepeat">반복</label>
        <select id="taskRepeat" class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3 text-sm">
          <option>매일 반복</option><option>평일 반복</option><option>주말 반복</option><option>사용자 지정</option><option>1회성</option>
        </select>
      </div>
      <div>
        <label class="text-xs font-bold" for="taskAlarm">알림</label>
        <input id="taskAlarm" type="time" class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3">
      </div>
      <div>
        <label class="text-xs font-bold" for="taskMemo">메모</label>
        <textarea id="taskMemo" class="w-full mt-1 bg-slate-50 border border-slate-200 rounded-xl px-3 py-3 text-sm" rows="3"></textarea>
      </div>
      <div class="flex gap-2">
        <button type="submit" class="flex-1 bg-sky-500 text-white font-bold rounded-xl py-3">저장</button>
        <button type="button" onclick="deleteActiveTask()" class="px-4 border border-rose-200 text-rose-500 rounded-xl">삭제</button>
      </div>
    </form>
  </aside>
</div>

<!-- Basic Info / Create Modal -->
<div id="routineFormModal" class="modal-bg fixed inset-0 bg-slate-900/50 z-50 items-center justify-center p-4" onclick="closeModal('routineFormModal',event)">
<div class="bg-white rounded-2xl w-full max-w-lg p-6" onclick="event.stopPropagation()">
  <div class="flex justify-between items-center mb-5">
    <h3 id="rfTitle" class="text-xl font-black">새 루틴 만들기</h3>
    <button type="button" onclick="hide('routineFormModal')" class="text-2xl text-slate-400">×</button>
  </div>
  <form onsubmit="saveRoutineInfo(event)" class="space-y-4">
    <div><label class="text-xs font-bold" for="rfName">루틴 이름 *</label><input id="rfName" required maxlength="50" class="w-full mt-1 border rounded-xl px-3 py-3"></div>
    <div><label class="text-xs font-bold" for="rfDesc">설명</label><textarea id="rfDesc" class="w-full mt-1 border rounded-xl px-3 py-3" rows="3"></textarea></div>
    <div>
      <label class="text-xs font-bold">루틴 기간 *</label>
      <div class="grid grid-cols-2 gap-2 mt-1">
        <input id="rfStart" type="date" required class="border rounded-xl px-3 py-3">
        <input id="rfEnd" type="date" required class="border rounded-xl px-3 py-3">
      </div>
    </div>
    <div>
      <label class="text-xs font-bold" for="rfRepeat">기본 반복</label>
      <select id="rfRepeat" class="w-full mt-1 border rounded-xl px-3 py-3">
        <option>매일</option><option>평일</option><option>주말</option><option>사용자 지정</option>
      </select>
    </div>
    <div class="flex gap-2">
      <button type="submit" class="flex-1 bg-sky-500 text-white py-3 rounded-xl font-bold">저장</button>
      <button type="button" onclick="hide('routineFormModal')" class="px-5 bg-slate-100 rounded-xl font-bold">취소</button>
    </div>
  </form>
</div></div>

<script>
function getActive(){ return routines.find(r=>r.id===activeRoutineId)||routines[0]; }
function dateLabel(s,e){ return s&&e ? s.replaceAll('-','.') + ' ~ ' + e.replaceAll('-','.') : '기간 미설정'; }
function completionKey(rid,tid,date){ return rid + '|' + tid + '|' + date; }
function taskId(t,r,i){ return t[3] || (r.id + '-T' + i); }
function isDone(r,t,i,date=selectedDate){ return !!completions[completionKey(r.id,taskId(t,r,i),date)]; }

function renderManage(){
    const r=getActive();
    document.getElementById('activeRoutineSelect').innerHTML=routines.map(function(x){ return '<option value="' + x.id + '" ' + (x.id===r.id?'selected':'') + '>' + x.name + '</option>'; }).join('');
    document.getElementById('routinePeriodLabel').textContent=dateLabel(r.start,r.end);
    document.getElementById('totalXp').textContent=totalXp.toLocaleString()+' XP';
    renderCalendar(); renderTimetable(); updateProgress();
}

function renderCalendar(){
    const grid=document.getElementById('calendarGrid'), title=document.getElementById('calendarTitle'), c=new Date(calendarCursor);
    if(calendarView==='week'){
        const day=c.getDay(), sun=new Date(c);
        sun.setDate(c.getDate()-day);
        const endDay = new Date(sun.getFullYear(),sun.getMonth(),sun.getDate()+6);
        title.textContent=sun.getFullYear() + '년 ' + (sun.getMonth()+1) + '월 ' + sun.getDate() + '일 ~ ' + (endDay.getMonth()+1) + '월 ' + endDay.getDate() + '일';
        let html='';
        for(let i=0;i<7;i++){ const d=new Date(sun); d.setDate(sun.getDate()+i); html+=calendarDayBox(d,false); }
        grid.innerHTML=html;
    }else{
        title.textContent=c.getFullYear() + '년 ' + (c.getMonth()+1) + '월';
        const first=new Date(c.getFullYear(),c.getMonth(),1), start=new Date(first);
        start.setDate(1-first.getDay());
        let html='';
        for(let i=0;i<42;i++){ const d=new Date(start); d.setDate(start.getDate()+i); html+=calendarDayBox(d,d.getMonth()!==c.getMonth()); }
        grid.innerHTML=html;
    }
}

function calendarDayBox(d,muted){
    const ds=d.toISOString().slice(0,10), r=getActive();
    const dots=r.tasks.slice(0,4).map(function(t,i){ return isDone(r,t,i,ds)?'<span class="dot done"></span>':'<span class="dot active"></span>'; }).join('');
    return '<div class="cal-day-box ' + (muted?'muted':'') + ' ' + (ds===selectedDate?'selected':'') + '" onclick="selectCalendarDate(\'' + ds + '\')">' +
           '<div class="cal-day-name">' + ['일','월','화','수','목','금','토'][d.getDay()] + '</div>' +
           '<div class="cal-date-num">' + d.getDate() + '</div>' +
           '<div class="cal-dots">' + dots + '</div></div>';
}

function selectCalendarDate(ds){ selectedDate=ds; calendarCursor=new Date(ds+'T00:00:00'); renderManage(); }
function moveCalendar(n){ if(calendarView==='week') calendarCursor.setDate(calendarCursor.getDate()+n*7); else calendarCursor.setMonth(calendarCursor.getMonth()+n); renderCalendar(); }
function toggleCalendarView(){ calendarView=calendarView==='week'?'month':'week'; document.getElementById('calendarViewBtn').textContent=calendarView==='week'?'주':'월'; renderCalendar(); }
function calendarToday(){ selectedDate=new Date().toISOString().slice(0,10); calendarCursor=new Date(selectedDate+'T00:00:00'); renderManage(); }

function renderTimetable(){
    const r=getActive(), box=document.getElementById('manageTimetable');
    document.getElementById('selectedDateLabel').textContent=selectedDate.replaceAll('-','.') + ' 기준 · Task를 클릭하면 상세 설정이 열립니다.';
    if(!r.tasks.length){ box.innerHTML='<div class="empty">등록된 Task가 없습니다.</div>'; return; }
    const sorted=[...r.tasks].sort((a,b)=>a[0].localeCompare(b[0]));
    box.innerHTML=sorted.map(function(t,i){
        const id=taskId(t,r,r.tasks.indexOf(t)), done=isDone(r,t,r.tasks.indexOf(t)), sel=id===selectedTaskId;
        const icon = (t[1].match(/\S+/)||[''])[0];
        const subInfo = t[2] + ' · ' + (t[4]||'매일 반복') + (t[5]?' · 🔔 '+t[5]:'') + (t[6]?' · 📍 '+t[6]:'');
        return '<div class="tt-row ' + (done?'done':'') + ' ' + (sel?'selected':'') + '" draggable="true" ondragstart="dragStart(event,\'' + id + '\')" ondragover="event.preventDefault()" ondrop="dropTask(event,\'' + id + '\')" onclick="selectTask(\'' + id + '\')">' +
               '<div class="flex items-start justify-center font-black text-slate-500">' + (i+1) + '</div>' +
               '<div class="font-black text-slate-700">' + t[0] + '</div>' +
               '<div class="flex items-center gap-3 min-w-0"><span class="text-slate-300 cursor-grab">≡</span><span class="text-xl">' + icon + '</span>' +
               '<div class="min-w-0"><div class="task-title font-bold">' + t[1] + '</div><div class="text-[10px] text-slate-400 mt-1">' + subInfo + '</div></div></div>' +
               '<div class="flex items-center justify-center"><button type="button" onclick="event.stopPropagation();toggleDone(\'' + id + '\')" class="w-9 h-9 rounded-full border ' + (done?'bg-emerald-500 border-emerald-500 text-white':'bg-white border-slate-300 text-slate-300') + '">' + (done?'✓':'○') + '</button></div>' +
               '</div>';
    }).join('');
}

function updateProgress(){ const r=getActive(), all=r.tasks.length, done=r.tasks.filter((t,i)=>isDone(r,t,i)).length; document.getElementById('progressRatio').textContent=all?Math.round(done/all*100)+'%':'0%'; }
function changeActiveRoutine(id){ activeRoutineId=id; persist(); selectedTaskId=null; calendarCursor=new Date(selectedDate+'T00:00:00'); renderManage(); }

function toggleDone(id){
    const r=getActive(), idx=r.tasks.findIndex((t,i)=>taskId(t,r,i)===id); if(idx<0) return;
    const k=completionKey(r.id,id,selectedDate);
    completions[k]=!completions[k];
    totalXp+=completions[k]?20:-20; totalXp=Math.max(0,totalXp);
    persist(); renderManage();
    toast(completions[k]?'루틴 완료! +20 XP':'완료를 취소했습니다.');
}

function selectTask(id){
    selectedTaskId=id; const r=getActive(), i=r.tasks.findIndex((t,j)=>taskId(t,r,j)===id); if(i<0) return;
    const t=r.tasks[i];
    document.getElementById('editingTaskId').value=id;
    document.getElementById('taskTitle').value=t[1];
    document.getElementById('taskTime').value=t[0];
    document.getElementById('taskCat').value=t[2];
    document.getElementById('taskLocation').value=t[6]||'';
    document.getElementById('taskRepeat').value=t[4]||'매일 반복';
    document.getElementById('taskAlarm').value=t[5]||'';
    document.getElementById('taskMemo').value=t[7]||'';
    renderTimetable();
}

function openTaskForm(){
    selectedTaskId=null; document.getElementById('taskForm').reset();
    document.getElementById('taskTime').value='12:00';
    document.getElementById('taskAlarm').value='12:00';
    document.getElementById('editingTaskId').value='NEW';
    renderTimetable();
}

function saveTask(e){
    e.preventDefault();
    const r=getActive(), id=document.getElementById('editingTaskId').value,
          newId = (id==='NEW'?'T'+Date.now():id),
          t=[document.getElementById('taskTime').value, document.getElementById('taskTitle').value.trim(), document.getElementById('taskCat').value, newId, document.getElementById('taskRepeat').value, document.getElementById('taskAlarm').value, document.getElementById('taskLocation').value.trim(), document.getElementById('taskMemo').value];
    if(!t[1]) return;
    if(r.tasks.some(x=>x[0]===t[0]&&x[3]!==id)&&!confirm('같은 시간에 다른 Task가 있습니다. 그래도 저장할까요?')) return;
    if(id==='NEW') r.tasks.push(t);
    else{ const i=r.tasks.findIndex((x,j)=>taskId(x,r,j)===id); if(i>=0) r.tasks[i]=t; }
    selectedTaskId=t[3]; persist(); renderManage();
    toast(id==='NEW'?'새 Task를 추가했습니다.':'Task를 수정했습니다.');
}

function deleteActiveTask(){
    const id=document.getElementById('editingTaskId').value;
    if(!id||id==='NEW') return toast('삭제할 Task를 선택하세요.');
    const r=getActive();
    if(r.tasks.length<=1) return toast('활성 루틴은 최소 1개의 Task가 필요합니다.');
    r.tasks=r.tasks.filter(x=>x[3]!==id); selectedTaskId=null; persist(); renderManage();
    toast('Task를 삭제했습니다.');
}

function dragStart(e,id){ e.dataTransfer.setData('text/plain',id); }
function dropTask(e,target){
    e.preventDefault();
    const dragId=e.dataTransfer.getData('text/plain'), r=getActive();
    const a=r.tasks.findIndex((x,i)=>taskId(x,r,i)===dragId), b=r.tasks.findIndex((x,i)=>taskId(x,r,i)===target);
    if(a<0||b<0) return;
    const tmp=r.tasks[a][0]; r.tasks[a][0]=r.tasks[b][0]; r.tasks[b][0]=tmp;
    persist(); renderManage(); toast('시간 순서를 변경했습니다.');
}

function openRoutineCreateModal(){
    document.getElementById('rfTitle').textContent='새 루틴 만들기';
    document.getElementById('rfName').value=''; document.getElementById('rfDesc').value='';
    document.getElementById('rfStart').value=new Date().toISOString().slice(0,10);
    let d=new Date(); d.setDate(d.getDate()+30);
    document.getElementById('rfEnd').value=d.toISOString().slice(0,10);
    document.getElementById('routineFormModal').classList.add('show');
}

function openRoutineInfo(){
    const r=getActive();
    document.getElementById('rfTitle').textContent='루틴 기본 정보';
    document.getElementById('rfName').value=r.name;
    document.getElementById('rfDesc').value=r.desc||'';
    document.getElementById('rfStart').value=r.start||'2026-06-01';
    document.getElementById('rfEnd').value=r.end||'2026-12-31';
    document.getElementById('routineFormModal').classList.add('show');
}

function saveRoutineInfo(e){
    e.preventDefault();
    const n=document.getElementById('rfName').value.trim(), s=document.getElementById('rfStart').value, en=document.getElementById('rfEnd').value;
    if(en<s) return toast('종료일은 시작일보다 빠를 수 없습니다.');
    if(document.getElementById('rfTitle').textContent==='새 루틴 만들기'){
        const id='R'+Date.now();
        routines.push({id,name:n,desc:document.getElementById('rfDesc').value,cat:['건강'],popular:0,newAt:Date.now(),difficulty:1,spotTypes:['주변 장소'],recommendedPlaces:[],fit:70,users:0,start:s,end:en,tasks:[['08:00','🌅 오늘의 핵심 행동','🏠 일상','T'+Date.now(),'매일 반복','','','']]});
        activeRoutineId=id;
    }else{
        const r=getActive(); r.name=n; r.desc=document.getElementById('rfDesc').value; r.start=s; r.end=en;
    }
    persist(); hide('routineFormModal'); renderExplore(); renderManage();
    toast('루틴 기본 정보를 저장했습니다.');
}
</script>