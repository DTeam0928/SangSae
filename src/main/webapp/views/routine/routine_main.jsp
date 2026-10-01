<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>일상을 새롭게, 상새</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/style-jsp.css">
<script src="https://cdn.tailwindcss.com"></script>
<script>
tailwind.config = {
  theme: {
    extend: {
      colors: {
        sky:   { 50:'#E4FBFF', 100:'#EAF7FF', 500:'#00A8FF', 600:'#0087dc' },
        amber: { 400:'#FFB142', 500:'#FFB142' },
      },
      fontFamily: { sans: ["'IBM Plex Sans KR'", 'Pretendard', 'sans-serif'] }
    }
  }
}
</script>

<style>
/* ===== 루틴 메인 페이지 고유 스타일 ===== */
body {
    background-color: var(--color-bg);
}

.logo-font {
    font-family: var(--font-logo);
    letter-spacing: 0.5px;
}

button, input, select, textarea { font: inherit; }

.tab-active {
    color: var(--color-dark);
    background: var(--color-main);
}

.modal-bg { display: none; }
.modal-bg.show { display: flex; }

.toast {
    opacity: 0;
    transform: translateY(12px);
    pointer-events: none;
    transition: opacity 0.2s, transform 0.2s;
}
.toast.show {
    opacity: 1;
    transform: none;
}
</style>
</head>
<body class="min-h-screen">

    <!-- 헤더 포함 -->
    <%@ include file="/views/common/header.jsp" %>

<main class="w-[min(1220px,calc(100%-28px))] mx-auto py-7 space-y-6">
  <!-- 탭 메뉴 컨트롤 -->
  <div class="flex gap-2 border-b-2 border-slate-200 pb-2">
    <button id="tabExplore" onclick="switchTab('explore')" class="tab-active px-4 py-2.5 rounded-xl font-bold text-base">✨ 루틴 탐색</button>
    <button id="tabManage" onclick="switchTab('manage')" class="px-4 py-2.5 rounded-xl font-bold text-base text-slate-400">📅 루틴 관리</button>
  </div>

  <!-- 탭 1: 루틴 탐색 -->
  <section id="tab-explore" class="space-y-6">
    <%@ include file="/views/routine/tab_explorer.jsp" %>
  </section>

  <!-- 탭 2: 루틴 관리 -->
  <section id="tab-manage" class="hidden space-y-6">
    <%@ include file="/views/routine/tab_management.jsp" %>
  </section>
</main>

<!-- 공통 토스트 알림 -->
<div id="toast" class="toast fixed bottom-6 right-6 bg-slate-800 text-white px-5 py-3 rounded-xl shadow-xl z-[100] text-sm"></div>

<script>
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
let routines = JSON.parse(localStorage.getItem('q_routines_v3')||'null') || ROUTINES.map(x=>structuredClone(x));
let activeRoutineId = localStorage.getItem('q_activeRoutine_v3') || 'R01';
let completions = JSON.parse(localStorage.getItem('q_completions_v3')||'{}');
let totalXp = +(localStorage.getItem('q_xp_v3')||1240);
let routineCategory='전체', selectedRoutine=null, selectedTaskId=null, selectedDate=new Date().toISOString().slice(0,10);
let calendarCursor=new Date(selectedDate+'T00:00:00'), calendarView='week', userLocation={lat:37.5665,lng:126.9780}, radiusKm=3, placeTypes=new Set(['헬스장','카페','공원','도서관','산책로','수영장']);

function persist(){
    localStorage.setItem('q_routines_v3',JSON.stringify(routines));
    localStorage.setItem('q_activeRoutine_v3',activeRoutineId);
    localStorage.setItem('q_completions_v3',JSON.stringify(completions));
    localStorage.setItem('q_xp_v3',totalXp);
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
    document.getElementById('tab-explore').classList.toggle('hidden', tab!=='explore');
    document.getElementById('tab-manage').classList.toggle('hidden', tab!=='manage');
    document.getElementById('tabExplore').className = tab==='explore' ? 'tab-active px-4 py-2.5 rounded-xl font-bold text-base' : 'px-4 py-2.5 rounded-xl font-bold text-base text-slate-400';
    document.getElementById('tabManage').className = tab==='manage' ? 'tab-active px-4 py-2.5 rounded-xl font-bold text-base' : 'px-4 py-2.5 rounded-xl font-bold text-base text-slate-400';
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
</script>
</body>
</html>