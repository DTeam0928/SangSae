(() => {
'use strict';

// ============================================================
// QUESTRIP ROUTINE - SPA & GLOBAL SCRIPTS
// ============================================================

const ROUTINES = [
 {id:'R01',name:'퇴근 후 헬스 60분 루틴',desc:'퇴근 후 바로 헬스장으로 이동해 워밍업-웨이트-유산소를 연결합니다.',cat:['운동','직장인'],popular:1840,newAt:18,difficulty:3,spotTypes:['헬스장'],recommendedPlaces:['P01','P02','P06'],fit:96,users:428,
  tasks:[['19:00','🚶 헬스장까지 10분 걷기','💪 운동'],['19:15','🔥 전신 워밍업 10분','💪 운동'],['19:30','🏋️ 하체 중심 웨이트 25분','💪 운동'],['20:00','🏃 러닝머신 15분','💪 운동'],['20:20','🧘 쿨다운 스트레칭','💪 운동']]},
 {id:'R02',name:'아침 카페 집중 루틴',desc:'집을 나와 카페에 앉는 행동 자체를 집중 시작 신호로 만드는 루틴입니다.',cat:['미라클 모닝','공부'],popular:1520,newAt:17,difficulty:2,spotTypes:['카페'],recommendedPlaces:['P03','P04'],fit:91,users:316,
  tasks:[['07:30','🚶 집에서 카페까지 걷기','💪 운동'],['08:00','☕ 카페 자리 정하기','🏠 일상'],['08:10','📚 오늘의 핵심 공부 50분','🧠 공부'],['09:00','✍️ 학습 로그 5분','📚 자기계발']]},
 {id:'R03',name:'한강 아침 러닝 루틴',desc:'공원과 강변을 활용해 짧은 러닝을 매일 반복합니다.',cat:['운동','건강'],popular:1390,newAt:16,difficulty:2,spotTypes:['공원','러닝트랙'],recommendedPlaces:['P05','P08'],fit:94,users:502,
  tasks:[['06:40','💧 물 한 컵 마시기','💧 건강'],['07:00','🏃 3km 가볍게 달리기','💪 운동'],['07:25','🧘 하체 스트레칭','💪 운동'],['07:35','📸 오늘의 러닝 기록','📚 자기계발']]},
 {id:'R04',name:'퇴근 후 도서관 몰입 루틴',desc:'도서관이라는 장소를 학습 시작 신호로 활용합니다.',cat:['공부','직장인'],popular:980,newAt:15,difficulty:3,spotTypes:['도서관'],recommendedPlaces:['P07'],fit:87,users:191,
  tasks:[['19:30','📚 오늘 공부 범위 확인','🧠 공부'],['19:40','🧠 50분 집중 세션','🧠 공부'],['20:30','☕ 10분 휴식','🏠 일상'],['20:40','✍️ 복습 노트 작성','📚 자기계발']]},
 {id:'R05',name:'헬스장 초보 전신 루틴',desc:'기구를 잘 몰라도 따라가기 쉬운 40분 전신 루틴입니다.',cat:['운동'],popular:910,newAt:14,difficulty:1,spotTypes:['헬스장'],recommendedPlaces:['P01','P06'],fit:98,users:274,
  tasks:[['18:00','🔥 러닝머신 워밍업 5분','💪 운동'],['18:05','🏋️ 레그프레스 3세트','💪 운동'],['18:15','🏋️ 랫풀다운 3세트','💪 운동'],['18:25','🏋️ 체스트프레스 3세트','💪 운동'],['18:35','🧘 전신 스트레칭','💪 운동']]},
 {id:'R06',name:'점심 20분 리셋 루틴',desc:'회사 주변 공원이나 산책로에서 짧게 걷고 오후를 준비합니다.',cat:['직장인','건강'],popular:760,newAt:13,difficulty:1,spotTypes:['공원','산책로'],recommendedPlaces:['P05','P08'],fit:89,users:143,
  tasks:[['12:20','🚶 10분 빠르게 걷기','💪 운동'],['12:30','🌿 벤치에서 호흡 3분','💧 건강'],['12:35','📝 오후 우선순위 3개','💼 업무']]},
 {id:'R07',name:'숙면을 위한 저녁 산책 루틴',desc:'집 주변 산책로를 잠들기 전 긴장 완화 장소로 활용합니다.',cat:['숙면','건강'],popular:640,newAt:12,difficulty:1,spotTypes:['산책로','공원'],recommendedPlaces:['P08'],fit:84,users:108,
  tasks:[['21:00','🚶 20분 천천히 걷기','💪 운동'],['21:20','🌙 오늘 좋았던 일 3개','📚 자기계발'],['21:30','📵 휴대폰 알림 끄기','🏠 일상']]},
 {id:'R08',name:'주말 브런치 + 독서 루틴',desc:'동네 카페에서 브런치 후 40분 독서로 주말을 시작합니다.',cat:['건강','공부'],popular:580,newAt:11,difficulty:2,spotTypes:['카페'],recommendedPlaces:['P03','P04'],fit:82,users:94,
  tasks:[['10:00','☕ 카페 도착','🏠 일상'],['10:20','🥪 가벼운 브런치','💧 건강'],['11:00','📖 책 40분 읽기','📚 자기계발']]},
 {id:'R09',name:'퇴근 후 수영장 루틴',desc:'수영장 방문 자체를 퇴근 후 운동 습관의 앵커로 만드는 루틴.',cat:['운동','건강'],popular:510,newAt:10,difficulty:3,spotTypes:['수영장'],recommendedPlaces:['P09'],fit:79,users:76,
  tasks:[['19:30','🏊 자유형 20분','💪 운동'],['19:55','🏊 인터벌 15분','💪 운동'],['20:15','🧘 어깨 스트레칭','💪 운동']]},
 {id:'R10',name:'출근 전 미라클 모닝',desc:'공원과 카페를 연속으로 활용해 아침 루틴을 외부 환경과 연결합니다.',cat:['미라클 모닝','건강'],popular:470,newAt:9,difficulty:2,spotTypes:['공원','카페'],recommendedPlaces:['P05','P03'],fit:86,users:71,
  tasks:[['06:30','🌅 10분 햇빛 걷기','💪 운동'],['06:45','☕ 카페에서 오늘 계획','💼 업무'],['07:05','📚 영어 20분','🧠 공부']]}
];

const PLACES = [
 {id:'P01',name:'시청 피트니스 랩',type:'헬스장',icon:'🏋️',lat:37.5666,lng:126.9784,address:'서울 중구 세종대로 인근',distance:0.8,hot:96,routines:18,tags:['웨이트','퇴근 운동','초보'],popular:['R01','R05'],trend:'퇴근 후 19~21시 이용 급증',x:50,y:45},
 {id:'P02',name:'을지로 웰니스짐',type:'헬스장',icon:'🏋️',lat:37.5663,lng:126.9912,address:'서울 중구 을지로 인근',distance:1.5,hot:91,routines:14,tags:['웨이트','러닝','직장인'],popular:['R01','R05'],trend:'직장인 루틴 저장 22% 증가',x:75,y:40},
 {id:'P03',name:'광화문 페이지 카페',type:'카페',icon:'☕',lat:37.5710,lng:126.9769,address:'서울 종로구 광화문 인근',distance:1.2,hot:89,routines:21,tags:['집중','독서','아침'],popular:['R02','R08','R10'],trend:'08~10시 집중 루틴 인기',x:45,y:25},
 {id:'P04',name:'정동 브루잉룸',type:'카페',icon:'☕',lat:37.5656,lng:126.9682,address:'서울 중구 정동 인근',distance:1.4,hot:84,routines:13,tags:['독서','노트북','주말'],popular:['R02','R08'],trend:'주말 독서 루틴 저장 증가',x:25,y:55},
 {id:'P05',name:'청계천 러닝존',type:'공원',icon:'🌳',lat:37.5685,lng:126.9890,address:'서울 종로구 청계천 인근',distance:1.0,hot:94,routines:27,tags:['러닝','산책','아침'],popular:['R03','R06','R10'],trend:'아침 러닝 인증 활발',x:68,y:30},
 {id:'P06',name:'남산 베이스짐',type:'헬스장',icon:'🏋️',lat:37.5549,lng:126.9818,address:'서울 중구 남산 인근',distance:2.2,hot:88,routines:16,tags:['웨이트','전신','초보'],popular:['R05','R01'],trend:'초보 전신 루틴 반응 높음',x:58,y:80},
 {id:'P07',name:'종로 시민도서관',type:'도서관',icon:'📚',lat:37.5758,lng:126.9863,address:'서울 종로구 인근',distance:1.7,hot:83,routines:12,tags:['집중','시험','독서'],popular:['R04'],trend:'평일 19시 이후 몰입 루틴 인기',x:62,y:15},
 {id:'P08',name:'한강 산책로 스팟',type:'산책로',icon:'🌿',lat:37.5280,lng:126.9340,address:'서울 용산구 한강 인근',distance:5.0,hot:90,routines:31,tags:['산책','숙면','러닝'],popular:['R03','R07','R06'],trend:'저녁 숙면 루틴 이용 증가',x:15,y:85},
 {id:'P09',name:'도심 아쿠아센터',type:'수영장',icon:'🏊',lat:37.5580,lng:126.9720,address:'서울 중구 인근',distance:1.9,hot:78,routines:9,tags:['수영','퇴근 운동'],popular:['R09'],trend:'평일 저녁 예약 루틴 증가',x:35,y:70}
];

const PLACE_IDEAS = [
 {place:'헬스장',title:'기구를 만나는 순간 운동 시작',desc:'도착 → 워밍업 → 핵심 운동 → 유산소 → 스트레칭',icon:'🏋️'},
 {place:'카페',title:'자리에 앉는 순간 집중 시작',desc:'착석 → 음료 → 50분 집중 → 기록 → 퇴실',icon:'☕'},
 {place:'공원',title:'걷는 장소를 회복 신호로',desc:'도착 → 걷기 → 호흡 → 오늘의 회고',icon:'🌳'},
 {place:'도서관',title:'공간을 몰입 앵커로',desc:'입실 → 자리 고정 → 집중 → 복습 → 체크아웃',icon:'📚'}
];

const MY_PLACES = ['P01','P03'];
let routines = App.storage.read('q_routines_v3', null);
if (!Array.isArray(routines) || !routines.length || !routines.every(r => r && typeof r.id === 'string' && typeof r.name === 'string' && Array.isArray(r.cat) && Array.isArray(r.recommendedPlaces) && Array.isArray(r.tasks) && r.tasks.every(t => Array.isArray(t) && typeof t[0] === 'string'))) routines = ROUTINES.map(x=>JSON.parse(JSON.stringify(x)));
let activeRoutineId = App.storage.raw('q_activeRoutine_v3') || 'R01';
let completions = App.storage.read('q_completions_v3', {});
let totalXp = +(App.storage.raw('q_xp_v3')||1240);
if (!Number.isFinite(totalXp)) totalXp=1240;
let routineCategory='전체', selectedRoutine=null, selectedTaskId=null, selectedDate=App.dateKey();
let calendarCursor=new Date(selectedDate+'T00:00:00'), calendarView='week', userLocation={lat:37.5665,lng:126.9780}, radiusKm=3, placeTypes=new Set(['헬스장','카페','공원','도서관','산책로','수영장']);

function persist(){
    App.storage.set('q_routines_v3',JSON.stringify(routines));
    App.storage.set('q_activeRoutine_v3',activeRoutineId);
    App.storage.set('q_completions_v3',JSON.stringify(completions));
    App.storage.set('q_xp_v3',totalXp);
}

function toast(m){
    const e=document.getElementById('toast');
    e.textContent=m;
    e.classList.add('show');
    clearTimeout(window.__toast);
    window.__toast=setTimeout(()=>e.classList.remove('show'),2300);
}

function hide(id){ document.getElementById(id).classList.remove('show'); }
function closeModal(id,e){ if(e.target.id===id) hide(id); }

function switchTab(tab){
    document.getElementById('tab-explore').classList.toggle('is-hidden', tab!=='explore');
    document.getElementById('tab-manage').classList.toggle('is-hidden', tab!=='manage');
    document.getElementById('tabExplore').className = tab==='explore' ? 'tab-active ds-px-4 ds-py-2_5 radius-md weight-bold type-base' : 'ds-px-4 ds-py-2_5 radius-md weight-bold type-base color-muted';
    document.getElementById('tabManage').className = tab==='manage' ? 'tab-active ds-px-4 ds-py-2_5 radius-md weight-bold type-base' : 'ds-px-4 ds-py-2_5 radius-md weight-bold type-base color-muted';
    if(tab==='manage') renderManage();
    if(tab==='explore') renderMapMarkers();
}

document.addEventListener('DOMContentLoaded', function() {
    routines.forEach(r=>{
        if(!r.start){ r.start='2026-06-01'; r.end='2026-12-31'; }
        r.tasks.forEach((t,i)=>{ if(!t[3]) t[3]=`${r.id}-T${i+1}`; });
    });
    persist();
    renderExplore();
    renderManage();
    renderMapMarkers();
});


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
  const catHtml = r.cat.map(function(c) { return '<span class="pill">' + App.escape(c) + '</span>'; }).join('');
  const placeName = p ? p.name : '주변 장소';

  return '<article class="card surface-white border-default border-color-border radius-lg clip-overflow cursor-pointer" onclick="App.routine.openRoutine(\'' + r.id + '\')">' +
         '<div class="ds-h-28 surface-gradient ds-p-4 layout-row justify-between">' +
         '<div class="layout-row ds-gap-1 layout-wrap align-start">' + catHtml + '</div>' +
         '<button type="button" onclick="event.stopPropagation();App.routine.saveRoutine(\'' + r.id + '\')" class="ds-w-9 ds-h-9 radius-pill surface-white color-muted">♡</button>' +
         '</div>' +
         '<div class="ds-p-5">' +
         '<h3 class="weight-heading type-lg">' + App.escape(r.name) + '</h3>' +
         '<p class="type-sm color-muted ds-mt-2 text-clamp-two">' + App.escape(r.desc) + '</p>' +
         '<div class="layout-row ds-gap-2 layout-wrap ds-mt-4">' +
         '<span class="pill">📍 ' + App.escape(placeName) + '</span>' +
         '<span class="pill">난이도 ' + r.difficulty + '/5</span>' +
         '</div>' +
         '<div class="layout-row justify-between align-center ds-mt-4 ds-pt-3 border-top border-color-sub-bg type-xs color-muted">' +
         '<span>Task ' + r.tasks.length + '개</span>' +
         '<span>🔥 ' + r.popular.toLocaleString() + '명</span>' +
         '</div>' +
         '</div>' +
         '</article>';
}

function renderExplore(){
    const a=filteredRoutines(), q=document.getElementById('routineSearch').value.trim();
    document.getElementById('searchState').classList.toggle('is-hidden',!q);
    document.getElementById('exploreHome').classList.toggle('is-hidden',!!q);
    if(q){
        document.getElementById('searchCount').textContent='"' + q + '" 검색 결과 ' + a.length + '개 · 루틴과 장소를 함께 탐색';
        document.getElementById('searchGrid').innerHTML=a.length?a.map(routineCard).join(''):'<div class="empty span-full">검색 결과가 없습니다.</div>';
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
    document.getElementById('rmBadges').innerHTML=selectedRoutine.cat.map(function(c){ return '<span class="pill">' + App.escape(c) + '</span>'; }).join('');
    document.getElementById('rmPlace').textContent=p?p.name:'주변 장소';
    document.getElementById('rmFit').textContent=selectedRoutine.fit+'%';
    document.getElementById('rmUsers').textContent=selectedRoutine.users.toLocaleString()+'명';
    document.getElementById('rmAll').checked=true;
    document.getElementById('rmTasks').innerHTML=selectedRoutine.tasks.map(function(t,i){
        return '<label class="layout-row align-center ds-gap-3 ds-p-3 surface-white border-default radius-md">' +
               '<input type="checkbox" checked class="rm-check input-accent" value="' + i + '">' +
               '<span class="font-monospace type-xs weight-bold">' + App.escape(t[0]) + '</span>' +
               '<span class="type-xs color-muted">' + App.escape(t[2]) + '</span>' +
               '<b class="type-sm">' + App.escape(t[1]) + '</b>' +
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
    let saved=App.storage.read('q_saved_routines_v3', []);
    saved=saved.includes(id)?saved.filter(x=>x!==id):[...saved,id];
    App.storage.set('q_saved_routines_v3',JSON.stringify(saved));
    toast(saved.includes(id)?'♡ 루틴을 저장했습니다.':'저장을 해제했습니다.');
}

function renderMapMarkers(){
  const container = document.getElementById('customMapContainer');
  if(!container) return;
  const nearby = PLACES.filter(p=>placeTypes.has(p.type)&&placeDistance(p)<=radiusKm);
  let html = '<div class="user-dot" style="left:50%;top:50%;" title="현재 위치"></div>';
  nearby.forEach(function(p){
    const bg = p.type==='헬스장'?'var(--color-main)':p.type==='카페'?'var(--color-sub-bg)':'var(--color-primary)';
    html += '<div class="map-pin" style="left:' + p.x + '%;top:' + p.y + '%;background:' + bg + '" onclick="App.routine.openPlace(\'' + p.id + '\')" title="' + App.escape(p.name) + ' (' + App.escape(p.type) + ')"><span>' + p.icon + '</span></div>';
  });
  container.innerHTML = html;
}

function renderNearbyPlaces(){
    const a=PLACES.filter(p=>placeTypes.has(p.type)).map(p=>({...p,d:placeDistance(p)})).filter(p=>p.d<=radiusKm).sort((a,b)=>a.d-b.d).slice(0,3);
    document.getElementById('nearbyPlaces').innerHTML=a.map(function(p){
        return '<div class="place-card" onclick="App.routine.openPlace(\'' + p.id + '\')">' +
               '<div class="layout-row justify-between"><div class="weight-heading">' + p.icon + ' ' + App.escape(p.name) + '</div><span class="pill">' + p.d.toFixed(1) + 'km</span></div>' +
               '<div class="type-xs color-muted ds-mt-2">' + App.escape(p.type) + ' · 핫도 ' + p.hot + '</div>' +
               '<div class="layout-row ds-gap-1 ds-mt-3">' + p.tags.map(function(t){ return '<span class="pill">' + t + '</span>'; }).join('') + '</div>' +
               '<div class="type-xs color-dark weight-bold ds-mt-3">' + App.escape(p.trend) + '</div>' +
               '</div>';
    }).join('')||'<div class="empty span-full">선택 반경 안에 장소가 없습니다. 반경을 넓혀보세요.</div>';
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
        return r ? '<div class="border-default radius-md ds-p-4 layout-row justify-between align-center"><div><b>' + App.escape(r.name) + '</b><div class="type-xs color-muted ds-mt-1">' + r.cat.join(' · ') + ' · ' + r.users.toLocaleString() + '명 수행</div></div><button type="button" onclick="App.routine.hide(\'placeModal\');App.routine.openRoutine(\'' + r.id + '\')" class="type-xs weight-bold color-dark">루틴 보기 →</button></div>' : '';
    }).join('');
    document.getElementById('placeModal').classList.add('show');
}

function renderMyPlaceSelect(){ const s=document.getElementById('myPlaceSelect'); s.innerHTML=MY_PLACES.map(function(id){const p=PLACES.find(x=>x.id===id); return '<option value="' + id + '">' + p.icon + ' ' + App.escape(p.name) + '</option>';}).join(''); }
function renderMyPlaceRoutines(){
    const id=document.getElementById('myPlaceSelect').value||MY_PLACES[0], p=PLACES.find(x=>x.id===id);
    document.getElementById('myPlaceRoutineList').innerHTML=p.popular.map(function(rid,i){
        const r=routines.find(x=>x.id===rid);
        return r ? '<div class="border-default radius-md ds-p-3 layout-row align-center justify-between"><div><div class="weight-bold type-sm">' + (i+1) + '. ' + App.escape(r.name) + '</div><div class="type-xs color-muted ds-mt-1">이 장소 수행자 ' + Math.round(r.users*(p.hot/100)).toLocaleString() + '명 · 적합도 ' + r.fit + '%</div></div><button type="button" onclick="App.routine.openRoutine(\'' + r.id + '\')" class="type-xs weight-bold color-dark">보기</button></div>' : '';
    }).join('');
}
function renderPlaceIdeas(){ document.getElementById('placeIdeas').innerHTML=PLACE_IDEAS.map(function(x){ return '<div class="layout-row ds-gap-3 ds-p-3 border-default radius-md"><div class="type-2xl">' + x.icon + '</div><div><b>' + x.place + ' · ' + x.title + '</b><div class="type-xs color-muted ds-mt-1">' + x.desc + '</div></div></div>'; }).join(''); }

function openPlaceFilters(){ document.getElementById('placeTypeFilters').innerHTML=['헬스장','카페','공원','도서관','산책로','수영장'].map(function(t){ return '<button type="button" class="day-chip ' + (placeTypes.has(t)?'active':'') + '" onclick="App.routine.togglePlaceType(\'' + t + '\',this)">' + iconFor(t) + ' ' + t + '</button>'; }).join(''); document.getElementById('filterModal').classList.add('show'); }
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
        renderNearbyPlaces(); renderMapMarkers(); renderExplore(); toast('현재 위치를 반영했습니다.');
    },()=>toast('위치 권한을 확인하지 못했습니다. 기본 위치로 계속합니다.'),{enableHighAccuracy:true,timeout:8000,maximumAge:60000});
}
function focusUser(){ toast('중심 위치로 이동했습니다.'); }
function openMyPlaces(){ toast('등록된 장소 관리 화면입니다.'); }
function showSection(){ document.getElementById('nearbyRoutineGrid').scrollIntoView({behavior:'smooth',block:'center'}); }


function getActive(){ return routines.find(r=>r.id===activeRoutineId)||routines[0]; }
function dateLabel(s,e){ return s&&e ? s.replaceAll('-','.') + ' ~ ' + e.replaceAll('-','.') : '기간 미설정'; }
function completionKey(rid,tid,date){ return rid + '|' + tid + '|' + date; }
function taskId(t,r,i){ return t[3] || (r.id + '-T' + i); }
function isDone(r,t,i,date=selectedDate){ return !!completions[completionKey(r.id,taskId(t,r,i),date)]; }

function renderManage(){
    const r=getActive();
    document.getElementById('activeRoutineSelect').innerHTML=routines.map(function(x){ return '<option value="' + x.id + '" ' + (x.id===r.id?'selected':'') + '>' + App.escape(x.name) + '</option>'; }).join('');
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
    const ds=App.dateKey(d), r=getActive();
    const dots=r.tasks.slice(0,4).map(function(t,i){ return isDone(r,t,i,ds)?'<span class="dot done"></span>':'<span class="dot active"></span>'; }).join('');
    return '<div class="cal-day-box ' + (muted?'muted':'') + ' ' + (ds===selectedDate?'selected':'') + '" onclick="App.routine.selectCalendarDate(\'' + ds + '\')">' +
           '<div class="cal-day-name">' + ['일','월','화','수','목','금','토'][d.getDay()] + '</div>' +
           '<div class="cal-date-num">' + d.getDate() + '</div>' +
           '<div class="cal-dots">' + dots + '</div></div>';
}

function selectCalendarDate(ds){ selectedDate=ds; calendarCursor=new Date(ds+'T00:00:00'); renderManage(); }
function moveCalendar(n){ if(calendarView==='week') calendarCursor.setDate(calendarCursor.getDate()+n*7); else { calendarCursor.setDate(1); calendarCursor.setMonth(calendarCursor.getMonth()+n); } renderCalendar(); }
function toggleCalendarView(){ calendarView=calendarView==='week'?'month':'week'; document.getElementById('calendarViewBtn').textContent=calendarView==='week'?'주':'월'; renderCalendar(); }
function calendarToday(){ selectedDate=App.dateKey(); calendarCursor=new Date(selectedDate+'T00:00:00'); renderManage(); }

function renderTimetable(){
    const r=getActive(), box=document.getElementById('manageTimetable');
    document.getElementById('selectedDateLabel').textContent=selectedDate.replaceAll('-','.') + ' 기준 · Task를 클릭하면 상세 설정이 열립니다.';
    if(!r.tasks.length){ box.innerHTML='<div class="empty">등록된 Task가 없습니다.</div>'; return; }
    const sorted=[...r.tasks].sort((a,b)=>a[0].localeCompare(b[0]));
    box.innerHTML=sorted.map(function(t,i){
        const id=taskId(t,r,r.tasks.indexOf(t)), done=isDone(r,t,r.tasks.indexOf(t)), sel=id===selectedTaskId;
        const icon = (t[1].match(/\S+/)||[''])[0];
        const subInfo = t[2] + ' · ' + (t[4]||'매일 반복') + (t[5]?' · 🔔 '+t[5]:'') + (t[6]?' · 📍 '+t[6]:'');
        return '<div class="tt-row ' + (done?'done':'') + ' ' + (sel?'selected':'') + '" draggable="true" ondragstart="App.routine.dragStart(event,\'' + id + '\')" ondragover="event.preventDefault()" ondrop="App.routine.dropTask(event,\'' + id + '\')" onclick="App.routine.selectTask(\'' + id + '\')">' +
               '<div class="layout-row align-start justify-center weight-heading color-muted">' + (i+1) + '</div>' +
               '<div class="weight-heading color-text">' + App.escape(t[0]) + '</div>' +
               '<div class="layout-row align-center ds-gap-3 min-width-zero"><span class="color-muted cursor-grab">≡</span><span class="type-xl">' + App.escape(icon) + '</span>' +
               '<div class="min-width-zero"><div class="task-title weight-bold">' + App.escape(t[1]) + '</div><div class="type-10px color-muted ds-mt-1">' + App.escape(subInfo) + '</div></div></div>' +
               '<div class="layout-row align-center justify-center"><button type="button" onclick="event.stopPropagation();App.routine.toggleDone(\'' + id + '\')" class="ds-w-9 ds-h-9 radius-pill border-default ' + (done?'surface-success border-color-success color-white':'surface-white border-color-muted color-muted') + '">' + (done?'✓':'○') + '</button></div>' +
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
    document.getElementById('rfStart').value=App.dateKey();
    let d=new Date(); d.setDate(d.getDate()+30);
    document.getElementById('rfEnd').value=App.dateKey(d);
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

document.addEventListener('DOMContentLoaded',()=>{if(location.hash==='#manage') switchTab('manage');});

App.routine = {persist, toast, hide, closeModal, switchTab, distanceKm, placeDistance, routineDistance, filteredRoutines, routineCard, renderExplore, clearSearch, setCategory, openRoutine, toggleModalTasks, importTasks, addAllRoutineTasks, addSelectedRoutineTasks, saveRoutine, renderMapMarkers, renderNearbyPlaces, openPlace, renderMyPlaceSelect, renderMyPlaceRoutines, renderPlaceIdeas, openPlaceFilters, iconFor, togglePlaceType, setRadius, applyPlaceFilters, locateUser, focusUser, openMyPlaces, showSection, getActive, dateLabel, completionKey, taskId, isDone, renderManage, renderCalendar, calendarDayBox, selectCalendarDate, moveCalendar, toggleCalendarView, calendarToday, renderTimetable, updateProgress, changeActiveRoutine, toggleDone, selectTask, openTaskForm, saveTask, deleteActiveTask, dragStart, dropTask, openRoutineCreateModal, openRoutineInfo, saveRoutineInfo};
})();
