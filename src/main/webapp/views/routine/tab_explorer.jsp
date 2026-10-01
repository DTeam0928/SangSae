<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<style>
/* ===== 루틴 탐색 탭 고유 스타일 ===== */
.map-wrap {
    height: 430px;
    border-radius: var(--radius-lg);
    overflow: hidden;
    position: relative;
    background: linear-gradient(135deg, #f0fdf4 0%, #e0f2fe 100%);
    border: 1px solid var(--color-border);
}

.map-grid-bg {
    position: absolute;
    inset: 0;
    background-image: radial-gradient(var(--color-dark) 1px, transparent 1px);
    background-size: 24px 24px;
    opacity: 0.15;
}

.map-pin {
    position: absolute;
    width: 36px; height: 36px;
    border-radius: 50% 50% 50% 0;
    transform: rotate(-45deg) translate(-50%, -50%);
    display: flex; align-items: center; justify-content: center;
    box-shadow: 0 4px 14px rgba(15,23,42,.22);
    border: 3px solid var(--color-white);
    cursor: pointer;
    transition: transform 0.2s;
}
.map-pin:hover { transform: rotate(-45deg) translate(-50%, -50%) scale(1.15); }
.map-pin span { transform: rotate(45deg); font-size: 15px; }

.user-dot {
    position: absolute; width: 18px; height: 18px;
    border-radius: 50%; background: #0ea5e9;
    border: 4px solid var(--color-white);
    box-shadow: 0 2px 10px #64748b;
    transform: translate(-50%, -50%);
    animation: pulse-ring 2s infinite;
}

@keyframes pulse-ring {
    0% { box-shadow: 0 0 0 0 rgba(14,165,233,0.4); }
    70% { box-shadow: 0 0 0 10px rgba(14,165,233,0); }
    100% { box-shadow: 0 0 0 0 rgba(14,165,233,0); }
}

.day-chip {
    border: 1px solid var(--color-border);
    border-radius: 11px;
    background: var(--color-white);
    padding: 7px 11px;
    font-size: var(--fs-small);
    font-weight: 700;
    cursor: pointer;
    white-space: nowrap;
}
.day-chip.active {
    border-color: var(--color-dark);
    background: var(--color-main);
    color: var(--color-text);
}

.place-card {
    border: 1px solid var(--color-border);
    border-radius: var(--radius-lg);
    background: var(--color-white);
    padding: 15px;
    cursor: pointer;
    transition: border-color 0.2s, box-shadow 0.2s;
}
.place-card.active {
    border-color: var(--color-dark);
    box-shadow: 0 0 0 2px var(--color-sub-bg);
}

.pill {
    font-size: 10px;
    font-weight: 800;
    padding: 3px 8px;
    border-radius: 999px;
    background: var(--color-main);
    color: var(--color-text);
    display: inline-block;
    line-height: normal;
}

.card {
    transition: transform 0.18s, box-shadow 0.18s;
}
.card:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-card);
}

.empty {
    padding: 35px;
    text-align: center;
    color: var(--color-muted);
    background: var(--color-bg);
    border: 1px dashed var(--color-border);
    border-radius: var(--radius-md);
}

@media(max-width:700px) {
    .map-wrap { height: 350px; }
}
</style>

<!-- 상단 배너 & 검색 -->
<section class="bg-gradient-to-br from-sky-500 to-cyan-500 text-white rounded-3xl p-6 md:p-8 shadow-sm">
  <div class="max-w-3xl">
    <div class="text-xs font-black tracking-widest text-sky-100">PLACE MAKES HABIT</div>
    <h1 class="text-3xl md:text-4xl font-black mt-2">장소가 습관을 만든다.</h1>
    <p class="mt-3 text-sm md:text-base text-sky-50 leading-7">루틴을 검색하는 것에서 끝나지 않습니다. 지금 있는 장소와 주변 스팟, 사람들이 실제로 수행하는 루틴을 연결해 오늘의 행동을 찾아보세요.</p>
  </div>
  <div class="mt-6 flex flex-col md:flex-row gap-3">
    <div class="relative flex-1">
      <span class="absolute left-4 top-3.5">🔍</span>
      <input id="routineSearch" oninput="renderExplore()" class="w-full rounded-xl pl-11 pr-10 py-3 text-slate-800 bg-white border-0 focus:outline-none" placeholder="루틴, 장소, 헬스장, 카페, 테마 검색">
      <button type="button" onclick="clearSearch()" class="absolute right-3 top-3 text-slate-400">×</button>
    </div>
    <button type="button" onclick="locateUser()" class="px-5 py-3 rounded-xl bg-white/15 border border-white/30 font-bold whitespace-nowrap">📍 내 위치 찾기</button>
  </div>
  <div class="mt-3 flex items-center gap-2 text-xs text-sky-50">
    <span id="locationStatus">기본 위치: 서울 시청 인근 · GPS를 사용하면 주변 추천이 정교해집니다.</span>
  </div>
</section>

<!-- 카테고리 필터 및 정렬 -->
<section class="bg-white border border-slate-200 rounded-2xl p-4 md:p-5 shadow-sm">
  <div class="flex flex-col lg:flex-row gap-3 lg:items-center">
    <div class="flex gap-2 overflow-x-auto scrollbar pb-1 flex-1">
      <button type="button" class="day-chip active" data-cat="전체" onclick="setCategory('전체',this)">🔥 전체</button>
      <button type="button" class="day-chip" data-cat="운동" onclick="setCategory('운동',this)">💪 운동</button>
      <button type="button" class="day-chip" data-cat="미라클 모닝" onclick="setCategory('미라클 모닝',this)">🌅 미라클 모닝</button>
      <button type="button" class="day-chip" data-cat="공부" onclick="setCategory('공부',this)">📚 공부</button>
      <button type="button" class="day-chip" data-cat="건강" onclick="setCategory('건강',this)">🥗 건강</button>
      <button type="button" class="day-chip" data-cat="숙면" onclick="setCategory('숙면',this)">😴 숙면</button>
      <button type="button" class="day-chip" data-cat="직장인" onclick="setCategory('직장인',this)">💼 직장인</button>
    </div>
    <select id="routineSort" onchange="renderExplore()" class="bg-slate-50 border border-slate-200 rounded-xl px-3 py-3 text-sm">
      <option value="near">내 위치 가까운 순</option>
      <option value="popular">인기순</option>
      <option value="new">최신순</option>
      <option value="easy">부담 적은 순</option>
    </select>
  </div>
</section>

<!-- 검색 결과 섹션 -->
<section id="searchState" class="hidden">
  <div class="flex justify-between items-end mb-3">
    <div>
      <h2 class="font-black text-xl">검색 결과</h2>
      <p id="searchCount" class="text-sm text-slate-400 mt-1"></p>
    </div>
    <button type="button" onclick="clearSearch()" class="text-sm text-sky-600 font-bold">탐색 홈으로</button>
  </div>
  <div id="searchGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"></div>
</section>

<!-- 탐색 홈 컨텐츠 -->
<div id="exploreHome" class="space-y-8">
  <section class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
    <div class="flex flex-col lg:flex-row lg:items-end justify-between gap-4 mb-4">
      <div>
        <h2 class="text-xl font-black">📍 지금 내 주변 루틴 스팟</h2>
        <p class="text-xs text-slate-400 mt-1">GPS 기준으로 가까운 장소와 그곳에서 많이 수행되는 루틴을 연결합니다.</p>
      </div>
      <div class="flex gap-2">
        <button type="button" onclick="focusUser()" class="px-3 py-2 rounded-xl bg-sky-50 text-sky-600 text-xs font-bold">내 위치로 이동</button>
        <button type="button" onclick="openPlaceFilters()" class="px-3 py-2 rounded-xl bg-slate-100 text-slate-600 text-xs font-bold">필터</button>
      </div>
    </div>
    <div id="map" class="map-wrap">
      <div class="map-grid-bg"></div>
      <div id="customMapContainer" class="absolute inset-0"></div>
      <div class="absolute bottom-3 left-3 bg-white/90 backdrop-blur-sm px-3 py-1.5 rounded-lg text-xs font-bold shadow-sm border border-slate-200 text-slate-600 z-10 flex items-center gap-2">
        <span class="w-2 h-2 rounded-full bg-sky-500 animate-pulse"></span> QUESTRIP 인터랙티브 지도 서비스
      </div>
    </div>
    <div id="nearbyPlaces" class="grid grid-cols-1 md:grid-cols-3 gap-3 mt-4"></div>
  </section>

  <section>
    <div class="flex justify-between items-end mb-3">
      <div>
        <h2 class="font-black text-xl">🔥 지금 이 근처에서 뜨는 루틴</h2>
        <p class="text-xs text-slate-400 mt-1">내 주변 장소에서 최근 많이 시작된 루틴</p>
      </div>
      <button type="button" onclick="showSection('nearby')" class="text-xs text-slate-400">더보기 →</button>
    </div>
    <div id="nearbyRoutineGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"></div>
  </section>

  <section class="grid grid-cols-1 lg:grid-cols-2 gap-6">
    <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
      <div class="flex justify-between items-start mb-4">
        <div>
          <h2 class="font-black text-xl">🏋️ 내가 다니는 장소의 인기 루틴</h2>
          <p class="text-xs text-slate-400 mt-1">등록한 장소에서 다른 사람들이 자주 하는 루틴</p>
        </div>
        <button type="button" onclick="openMyPlaces()" class="text-xs text-sky-600 font-bold">장소 관리</button>
      </div>
      <select id="myPlaceSelect" onchange="renderMyPlaceRoutines()" class="w-full border border-slate-200 bg-slate-50 rounded-xl px-3 py-3 text-sm"></select>
      <div id="myPlaceRoutineList" class="space-y-3 mt-4"></div>
    </div>
    <div class="bg-white border border-slate-200 rounded-2xl p-5 shadow-sm">
      <div class="flex justify-between items-start mb-4">
        <div>
          <h2 class="font-black text-xl">🎯 장소별 루틴 아이디어</h2>
          <p class="text-xs text-slate-400 mt-1">같은 행동도 장소에 따라 다른 루틴이 됩니다.</p>
        </div>
      </div>
      <div id="placeIdeas" class="space-y-3"></div>
    </div>
  </section>

  <section>
    <div class="flex justify-between items-end mb-3">
      <div>
        <h2 class="font-black text-xl">💡 당신에게 추천</h2>
        <p class="text-xs text-slate-400 mt-1">저장·완료 기록과 현재 위치를 함께 반영한 추천</p>
      </div>
    </div>
    <div id="recommendedGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"></div>
  </section>

  <section>
    <div class="flex justify-between items-end mb-3">
      <div>
        <h2 class="font-black text-xl">🔥 전국 인기 루틴</h2>
        <p class="text-xs text-slate-400 mt-1">장소와 함께 많이 저장된 루틴</p>
      </div>
    </div>
    <div id="popularGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"></div>
  </section>

  <section>
    <div class="flex justify-between items-end mb-3">
      <div>
        <h2 class="font-black text-xl">🆕 새로 올라온 루틴</h2>
        <p class="text-xs text-slate-400 mt-1">최근 등록된 장소 기반 루틴</p>
      </div>
    </div>
    <div id="newGrid" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5"></div>
  </section>
</div>

<!-- Routine Detail Modal -->
<div id="routineModal" class="modal-bg fixed inset-0 bg-slate-900/50 z-50 items-center justify-center p-4" onclick="closeModal('routineModal',event)">
<div class="bg-white rounded-2xl w-full max-w-2xl max-h-[92vh] overflow-hidden" onclick="event.stopPropagation()">
  <div class="p-6 border-b flex justify-between">
    <div>
      <div id="rmBadges" class="flex gap-2 mb-2"></div>
      <h3 id="rmTitle" class="text-2xl font-black"></h3>
      <p id="rmDesc" class="text-sm text-slate-500 mt-1"></p>
    </div>
    <button type="button" onclick="hide('routineModal')" class="text-2xl text-slate-400">×</button>
  </div>
  <div class="p-6 overflow-y-auto max-h-[64vh] bg-slate-50">
    <div class="grid grid-cols-1 md:grid-cols-3 gap-3 mb-4">
      <div class="bg-white rounded-xl p-3 border"><div class="text-[10px] text-slate-400">추천 장소</div><b id="rmPlace"></b></div>
      <div class="bg-white rounded-xl p-3 border"><div class="text-[10px] text-slate-400">장소 적합도</div><b id="rmFit"></b></div>
      <div class="bg-white rounded-xl p-3 border"><div class="text-[10px] text-slate-400">주변 수행자</div><b id="rmUsers"></b></div>
    </div>
    <div class="flex justify-between mb-3">
      <b>포함 Task</b>
      <label class="text-xs"><input id="rmAll" type="checkbox" checked onchange="toggleModalTasks(this)"> 전체 선택</label>
    </div>
    <div id="rmTasks" class="space-y-2"></div>
  </div>
  <div class="p-5 border-t flex gap-2">
    <button type="button" onclick="addSelectedRoutineTasks()" class="flex-1 py-3 border rounded-xl font-bold">선택 Task 담기</button>
    <button type="button" onclick="addAllRoutineTasks()" class="flex-1 py-3 bg-sky-500 text-white rounded-xl font-bold">전체 루틴 담기</button>
  </div>
</div></div>

<!-- Place Detail Modal -->
<div id="placeModal" class="modal-bg fixed inset-0 bg-slate-900/50 z-50 items-center justify-center p-4" onclick="closeModal('placeModal',event)">
<div class="bg-white rounded-2xl w-full max-w-xl max-h-[90vh] overflow-hidden" onclick="event.stopPropagation()">
  <div class="p-6 border-b flex justify-between">
    <div>
      <div class="text-xs font-bold text-sky-500" id="pmType"></div>
      <h3 class="text-2xl font-black mt-1" id="pmName"></h3>
      <p class="text-sm text-slate-500 mt-1" id="pmAddress"></p>
    </div>
    <button type="button" onclick="hide('placeModal')" class="text-2xl text-slate-400">×</button>
  </div>
  <div class="p-6 overflow-y-auto max-h-[60vh]">
    <div class="grid grid-cols-3 gap-3 mb-5">
      <div class="bg-slate-50 rounded-xl p-3"><div class="text-[10px] text-slate-400">거리</div><b id="pmDistance"></b></div>
      <div class="bg-slate-50 rounded-xl p-3"><div class="text-[10px] text-slate-400">루틴 수</div><b id="pmRoutineCount"></b></div>
      <div class="bg-slate-50 rounded-xl p-3"><div class="text-[10px] text-slate-400">핫도</div><b id="pmHot"></b></div>
    </div>
    <h4 class="font-black mb-3">이 장소에서 인기 있는 루틴</h4>
    <div id="pmRoutines" class="space-y-3"></div>
  </div>
  <div class="p-5 border-t">
    <button type="button" onclick="hide('placeModal')" class="w-full py-3 bg-sky-500 text-white rounded-xl font-bold">확인</button>
  </div>
</div></div>

<!-- Filters Modal -->
<div id="filterModal" class="modal-bg fixed inset-0 bg-slate-900/50 z-50 items-center justify-center p-4" onclick="closeModal('filterModal',event)">
<div class="bg-white rounded-2xl w-full max-w-lg p-6" onclick="event.stopPropagation()">
  <div class="flex justify-between items-center">
    <h3 class="text-xl font-black">주변 장소 필터</h3>
    <button type="button" onclick="hide('filterModal')" class="text-2xl text-slate-400">×</button>
  </div>
  <div class="space-y-4 mt-5">
    <div><label class="text-xs font-bold">장소 유형</label><div id="placeTypeFilters" class="flex flex-wrap gap-2 mt-2"></div></div>
    <div>
      <label class="text-xs font-bold">반경</label>
      <div class="flex gap-2 mt-2">
        <button type="button" class="day-chip" data-radius="1" onclick="setRadius(1,this)">1km</button>
        <button type="button" class="day-chip active" data-radius="3" onclick="setRadius(3,this)">3km</button>
        <button type="button" class="day-chip" data-radius="5" onclick="setRadius(5,this)">5km</button>
        <button type="button" class="day-chip" data-radius="10" onclick="setRadius(10,this)">10km</button>
      </div>
    </div>
  </div>
  <button type="button" onclick="applyPlaceFilters()" class="w-full mt-6 py-3 bg-sky-500 text-white rounded-xl font-bold">적용</button>
</div></div>

<script>
function distanceKm(a,b,c,d){ const R=6371,rad=Math.PI/180; const x=(c-a)*rad,y=(d-b)*rad; const h=Math.sin(x/2)**2+Math.cos(a*rad)*Math.cos(c*rad)*Math.sin(y/2)**2; return R*2*Math.atan2(Math.sqrt(h),Math.sqrt(1-h)); }
function placeDistance(p){ return distanceKm(userLocation.lat,userLocation.lng,p.lat,p.lng); }
function routineDistance(r){ const ps=PLACES.filter(p=>r.recommendedPlaces.includes(p.id)); return ps.length?Math.min(...ps.map(placeDistance)):99; }

function filteredRoutines(){
    const q=document.getElementById('routineSearch').value.trim().toLowerCase();
    let a=routines.filter(r=>(routineCategory==='전체'||r.cat.includes(routineCategory))&&(!q||(r.name+' '+r.desc+' '+r.cat.join(' ')+' '+r.spotTypes.join(' ')).toLowerCase().includes(q)));
    const s=document.getElementById('routineSort').value;
    if(s==='near') a.sort((x,y)=>routineDistance(x)-routineDistance(y));
    if(s==='popular') a.sort((x,y)=>y.popular-x.popular);
    if(s==='new') a.sort((x,y)=>y.newAt-x.newAt);
    if(s==='easy') a.sort((x,y)=>x.difficulty-y.difficulty);
    return a;
}

function routineCard(r){
  const p=PLACES.find(x=>r.recommendedPlaces.includes(x.id));
  const catHtml = r.cat.map(function(c) { return '<span class="pill">' + c + '</span>'; }).join('');
  const placeName = p ? p.name : '주변 장소';

  return '<article class="card bg-white border border-slate-200 rounded-2xl overflow-hidden cursor-pointer" onclick="openRoutine(\'' + r.id + '\')">' +
         '<div class="h-28 bg-gradient-to-br from-sky-50 to-cyan-50 p-4 flex justify-between">' +
         '<div class="flex gap-1 flex-wrap items-start">' + catHtml + '</div>' +
         '<button type="button" onclick="event.stopPropagation();saveRoutine(\'' + r.id + '\')" class="w-9 h-9 rounded-full bg-white text-slate-400">♡</button>' +
         '</div>' +
         '<div class="p-5">' +
         '<h3 class="font-black text-lg">' + r.name + '</h3>' +
         '<p class="text-sm text-slate-500 mt-2 line-clamp-2">' + r.desc + '</p>' +
         '<div class="flex gap-2 flex-wrap mt-4">' +
         '<span class="pill">📍 ' + placeName + '</span>' +
         '<span class="pill">난이도 ' + r.difficulty + '/5</span>' +
         '</div>' +
         '<div class="flex justify-between items-center mt-4 pt-3 border-t border-slate-100 text-xs text-slate-400">' +
         '<span>Task ' + r.tasks.length + '개</span>' +
         '<span>🔥 ' + r.popular.toLocaleString() + '명</span>' +
         '</div>' +
         '</div>' +
         '</article>';
}

function renderExplore(){
    const a=filteredRoutines(), q=document.getElementById('routineSearch').value.trim();
    document.getElementById('searchState').classList.toggle('hidden',!q);
    document.getElementById('exploreHome').classList.toggle('hidden',!!q);
    if(q){
        document.getElementById('searchCount').textContent='"' + q + '" 검색 결과 ' + a.length + '개 · 루틴과 장소를 함께 탐색';
        document.getElementById('searchGrid').innerHTML=a.length?a.map(routineCard).join(''):'<div class="empty col-span-full">검색 결과가 없습니다.</div>';
        return;
    }
    document.getElementById('nearbyRoutineGrid').innerHTML=a.slice(0,3).map(routineCard).join('');
    document.getElementById('recommendedGrid').innerHTML=[...routines].sort((x,y)=>y.fit-x.fit).slice(0,3).map(routineCard).join('');
    document.getElementById('popularGrid').innerHTML=[...routines].sort((x,y)=>y.popular-x.popular).slice(0,3).map(routineCard).join('');
    document.getElementById('newGrid').innerHTML=[...routines].sort((x,y)=>y.newAt-x.newAt).slice(0,3).map(routineCard).join('');
    renderNearbyPlaces(); renderMyPlaceSelect(); renderMyPlaceRoutines(); renderPlaceIdeas(); renderMapMarkers();
}

function clearSearch(){ document.getElementById('routineSearch').value=''; renderExplore(); }
function setCategory(c,b){ routineCategory=c; document.querySelectorAll('[data-cat]').forEach(x=>x.classList.remove('active')); b.classList.add('active'); renderExplore(); }

function openRoutine(id){
    selectedRoutine=routines.find(r=>r.id===id);
    if(!selectedRoutine) return;
    const p=PLACES.find(x=>selectedRoutine.recommendedPlaces.includes(x.id));
    document.getElementById('rmTitle').textContent=selectedRoutine.name;
    document.getElementById('rmDesc').textContent=selectedRoutine.desc;
    document.getElementById('rmBadges').innerHTML=selectedRoutine.cat.map(function(c){ return '<span class="pill">' + c + '</span>'; }).join('');
    document.getElementById('rmPlace').textContent=p?p.name:'주변 장소';
    document.getElementById('rmFit').textContent=selectedRoutine.fit+'%';
    document.getElementById('rmUsers').textContent=selectedRoutine.users.toLocaleString()+'명';
    document.getElementById('rmAll').checked=true;
    document.getElementById('rmTasks').innerHTML=selectedRoutine.tasks.map(function(t,i){
        return '<label class="flex items-center gap-3 p-3 bg-white border rounded-xl">' +
               '<input type="checkbox" checked class="rm-check accent-sky-500" value="' + i + '">' +
               '<span class="font-mono text-xs font-bold">' + t[0] + '</span>' +
               '<span class="text-xs text-slate-400">' + t[2] + '</span>' +
               '<b class="text-sm">' + t[1] + '</b>' +
               '</label>';
    }).join('');
    document.getElementById('routineModal').classList.add('show');
}

function toggleModalTasks(c){ document.querySelectorAll('.rm-check').forEach(x=>x.checked=c.checked); }

function importTasks(list){
    const r=getActive();
    list.forEach(t=>r.tasks.push([t[0],t[1],t[2],'T' + Date.now() + Math.random().toString(16).slice(2),'매일 반복','','']));
    persist(); hide('routineModal'); switchTab('manage');
    toast(list.length + '개 Task를 내 루틴에 추가했습니다.');
}

function addAllRoutineTasks(){ importTasks(selectedRoutine.tasks); }
function addSelectedRoutineTasks(){ const cs=[...document.querySelectorAll('.rm-check:checked')]; if(!cs.length) return toast('선택한 Task가 없습니다.'); importTasks(cs.map(x=>selectedRoutine.tasks[+x.value])); }

function saveRoutine(id){
    let saved=JSON.parse(localStorage.getItem('q_saved_routines_v3')||'[]');
    saved=saved.includes(id)?saved.filter(x=>x!==id):[...saved,id];
    localStorage.setItem('q_saved_routines_v3',JSON.stringify(saved));
    toast(saved.includes(id)?'♡ 루틴을 저장했습니다.':'저장을 해제했습니다.');
}

function renderMapMarkers(){
  const container = document.getElementById('customMapContainer');
  if(!container) return;
  const nearby = PLACES.filter(p=>placeTypes.has(p.type)&&placeDistance(p)<=radiusKm);
  let html = '<div class="user-dot" style="left:50%;top:50%;" title="현재 위치"></div>';
  nearby.forEach(function(p){
    const bg = p.type==='헬스장'?'#e0f2fe':p.type==='카페'?'#fef3c7':'#dcfce7';
    html += '<div class="map-pin" style="left:' + p.x + '%;top:' + p.y + '%;background:' + bg + '" onclick="openPlace(\'' + p.id + '\')" title="' + p.name + ' (' + p.type + ')"><span>' + p.icon + '</span></div>';
  });
  container.innerHTML = html;
}

function renderNearbyPlaces(){
    const a=PLACES.filter(p=>placeTypes.has(p.type)).map(p=>({...p,d:placeDistance(p)})).filter(p=>p.d<=radiusKm).sort((a,b)=>a.d-b.d).slice(0,3);
    document.getElementById('nearbyPlaces').innerHTML=a.map(function(p){
        return '<div class="place-card" onclick="openPlace(\'' + p.id + '\')">' +
               '<div class="flex justify-between"><div class="font-black">' + p.icon + ' ' + p.name + '</div><span class="pill">' + p.d.toFixed(1) + 'km</span></div>' +
               '<div class="text-xs text-slate-400 mt-2">' + p.type + ' · 핫도 ' + p.hot + '</div>' +
               '<div class="flex gap-1 mt-3">' + p.tags.map(function(t){ return '<span class="pill">' + t + '</span>'; }).join('') + '</div>' +
               '<div class="text-xs text-sky-600 font-bold mt-3">' + p.trend + '</div>' +
               '</div>';
    }).join('')||'<div class="empty col-span-full">선택 반경 안에 장소가 없습니다. 반경을 넓혀보세요.</div>';
}

function openPlace(id){
    const p=PLACES.find(x=>x.id===id); if(!p) return;
    document.getElementById('pmType').textContent=p.type;
    document.getElementById('pmName').textContent=p.icon+' '+p.name;
    document.getElementById('pmAddress').textContent=p.address;
    document.getElementById('pmDistance').textContent=placeDistance(p).toFixed(1)+' km';
    document.getElementById('pmRoutineCount').textContent=p.routines+'개';
    document.getElementById('pmHot').textContent=p.hot+'점';
    document.getElementById('pmRoutines').innerHTML=p.popular.map(function(id){
        const r=routines.find(x=>x.id===id);
        return r ? '<div class="border rounded-xl p-4 flex justify-between items-center"><div><b>' + r.name + '</b><div class="text-xs text-slate-400 mt-1">' + r.cat.join(' · ') + ' · ' + r.users.toLocaleString() + '명 수행</div></div><button type="button" onclick="hide(\'placeModal\');openRoutine(\'' + r.id + '\')" class="text-xs font-bold text-sky-600">루틴 보기 →</button></div>' : '';
    }).join('');
    document.getElementById('placeModal').classList.add('show');
}

function renderMyPlaceSelect(){ const s=document.getElementById('myPlaceSelect'); s.innerHTML=MY_PLACES.map(function(id){const p=PLACES.find(x=>x.id===id); return '<option value="' + id + '">' + p.icon + ' ' + p.name + '</option>';}).join(''); }
function renderMyPlaceRoutines(){
    const id=document.getElementById('myPlaceSelect').value||MY_PLACES[0], p=PLACES.find(x=>x.id===id);
    document.getElementById('myPlaceRoutineList').innerHTML=p.popular.map(function(rid,i){
        const r=routines.find(x=>x.id===rid);
        return r ? '<div class="border rounded-xl p-3 flex items-center justify-between"><div><div class="font-bold text-sm">' + (i+1) + '. ' + r.name + '</div><div class="text-xs text-slate-400 mt-1">이 장소 수행자 ' + Math.round(r.users*(p.hot/100)).toLocaleString() + '명 · 적합도 ' + r.fit + '%</div></div><button type="button" onclick="openRoutine(\'' + r.id + '\')" class="text-xs font-bold text-sky-600">보기</button></div>' : '';
    }).join('');
}
function renderPlaceIdeas(){ document.getElementById('placeIdeas').innerHTML=PLACE_IDEAS.map(function(x){ return '<div class="flex gap-3 p-3 border rounded-xl"><div class="text-2xl">' + x.icon + '</div><div><b>' + x.place + ' · ' + x.title + '</b><div class="text-xs text-slate-400 mt-1">' + x.desc + '</div></div></div>'; }).join(''); }

function openPlaceFilters(){ document.getElementById('placeTypeFilters').innerHTML=['헬스장','카페','공원','도서관','산책로','수영장'].map(function(t){ return '<button type="button" class="day-chip ' + (placeTypes.has(t)?'active':'') + '" onclick="togglePlaceType(\'' + t + '\',this)">' + iconFor(t) + ' ' + t + '</button>'; }).join(''); document.getElementById('filterModal').classList.add('show'); }
function iconFor(type){ return type==='헬스장'?'🏋':type==='카페'?'☕':type==='공원'?'🌳':type==='도서관'?'📚':type==='수영장'?'🏊':'🌿'; }
function togglePlaceType(t,b){ if(placeTypes.has(t)) placeTypes.delete(t); else placeTypes.add(t); b.classList.toggle('active'); }
function setRadius(n,b){ radiusKm=n; document.querySelectorAll('[data-radius]').forEach(x=>x.classList.remove('active')); b.classList.add('active'); }
function applyPlaceFilters(){ hide('filterModal'); renderNearbyPlaces(); renderMapMarkers(); toast('주변 ' + radiusKm + 'km · ' + placeTypes.size + '개 장소 유형 필터 적용'); }

function locateUser(){
    if(!navigator.geolocation){ toast('브라우저가 GPS를 지원하지 않아 기본 위치를 사용합니다.'); return; }
    toast('현재 위치를 확인하는 중입니다…');
    navigator.geolocation.getCurrentPosition(pos=>{
        userLocation={lat:pos.coords.latitude,lng:pos.coords.longitude};
        document.getElementById('locationStatus').textContent='GPS 위치 사용 중 · 정확도 약 ' + Math.round(pos.coords.accuracy) + 'm';
        renderNearbyPlaces(); renderExplore(); toast('현재 위치를 반영했습니다.');
    },()=>toast('위치 권한을 확인하지 못했습니다. 기본 위치로 계속합니다.'),{enableHighAccuracy:true,timeout:8000,maximumAge:60000});
}
function focusUser(){ toast('중심 위치로 이동했습니다.'); }
function openMyPlaces(){ toast('등록된 장소 관리 화면입니다.'); }
function showSection(){ document.getElementById('nearbyRoutineGrid').scrollIntoView({behavior:'smooth',block:'center'}); }
</script>