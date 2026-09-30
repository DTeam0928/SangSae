<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>QUESTRIP - 퀘스트</title>

    <!-- 공통 CSS 로드 -->
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style-jsp.css">

    <style>
        /* Q-00 전용 메인 탭 스타일 */
        .main-tabs {
            display: flex;
            gap: 8px;
            margin-bottom: 24px;
            border-bottom: 2px solid var(--color-border2);
            padding-bottom: 10px;
        }
        .main-tab-btn {
            border: 0;
            background: transparent;
            color: var(--color-muted);
            padding: 10px 17px;
            border-radius: var(--radius-sm);
            cursor: pointer;
        }
        .main-tab-btn.active {
            background: var(--color-main);
            color: var(--color-dark);
        }
        .tab-content { display: none; }
        .tab-content.active { display: block; }

        /* 공통 뱃지 그룹 */
        .badges { display: flex; gap: 6px; flex-wrap: wrap; margin-bottom: 9px; }

        /* 모달 백드롭 및 모달 스타일 */
        .modal-bg {
            position: fixed; inset: 0; background: rgba(18, 38, 48, 0.6);
            display: none; align-items: center; justify-content: center; padding: 15px; z-index: 200;
        }
        .modal-bg.show { display: flex; }
        .modal {
            background: var(--color-white); border-radius: var(--radius-lg);
            width: min(760px, 100%); max-height: 90vh; overflow-y: auto; padding: 25px;
            box-shadow: 0 20px 60px rgba(0,0,0,0.18);
        }
        .modal-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20px; }
        .modal-head h2 { margin: 0; font-size: var(--fs-h2); font-weight: var(--fw-h2); }
        .close { border: 0; background: var(--color-sub-bg); border-radius: 8px; width: 34px; height: 34px; font-size: 19px; cursor: pointer; }

        /* 토스트 알림 */
        .toast {
            position: fixed;
            right: 24px;
            bottom: 24px;
            background: var(--color-text);
            color: var(--color-white);
            padding: 14px 18px;
            border-radius: var(--radius-md);
            opacity: 0;
            transform: translateY(12px);
            transition: .3s;
            z-index: 300;
            pointer-events: none;
            display: flex;
            align-items: center;
            gap: 8px;
        }
        .toast.show { opacity: 1; transform: none; }

        /* 컨텍스트 메뉴 */
        .context-menu {
            position: fixed;
            background: var(--color-white);
            border: 1px solid var(--color-border);
            box-shadow: var(--shadow-card);
            border-radius: var(--radius-sm);
            padding: 5px;
            z-index: 150;
            display: none;
        }
        .context-menu button {
            display: block;
            width: 140px;
            text-align: left;
            border: 0;
            background: var(--color-white);
            padding: 10px;
            border-radius: 6px;
            cursor: pointer;
        }
        .context-menu button:hover { background: var(--color-bg); }
        .context-menu button.danger { color: var(--color-danger); }

        .collection-row { display: flex; gap: 12px; align-items: center; padding: 12px 0; border-bottom: 1px solid var(--color-border2); }
        .collection-row:last-child { border-bottom: 0; }
    </style>
</head>
<body>

    <!-- 헤더 포함 -->
    <%@ include file="/views/common/header.jsp" %>

    <main class="container">
        <!-- 메인 탭 전환 버튼 -->
        <div class="main-tabs">
            <button class="main-tab-btn title active" onclick="switchTab('explorer', this)">퀘스트 탐색</button>
            <button class="main-tab-btn title" onclick="switchTab('management', this)">
                여행 관리 <span id="plannedCountBadge" class="desc"></span>
            </button>
        </div>

        <!-- [탭 1] 퀘스트 탐색 탭 -->
        <div id="tab-explorer" class="tab-content active">
            <%@ include file="/views/quest/tab_explorer.jsp" %>
        </div>

        <!-- [탭 2] 여행 관리 탭 (올바른 경로 적용) -->
        <div id="tab-management" class="tab-content">
            <%@ include file="/views/quest/tab_management.jsp" %>
        </div>
    </main>

    <!-- ==========================================
         인라인 모달 레이어 정의
    =========================================== -->
    <!-- 1. 지역 선택 모달 -->
    <div class="modal-bg" id="regionModal" onclick="closeModalOutside(event, 'regionModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">탐색할 지역 선택</h2>
          <button class="close" onclick="closeModal('regionModal')">×</button>
        </div>
        <div style="padding:15px; text-align:center;">
          <p class="desc">부산 지역 필터가 기본 적용되어 있습니다.</p>
          <button class="btn btn-primary" onclick="applyRegions()">부산 전체 적용</button>
        </div>
      </div>
    </div>

    <!-- 2. 내 컬렉션 모달 -->
    <div class="modal-bg" id="collectionModal" onclick="closeModalOutside(event, 'collectionModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">♡ 내 컬렉션</h2>
          <button class="close" onclick="closeModal('collectionModal')">×</button>
        </div>
        <div id="collectionBody"></div>
      </div>
    </div>

    <!-- 3. 여행 선택/전환 모달 -->
    <div class="modal-bg" id="tripModal" onclick="closeModalOutside(event, 'tripModal')">
      <div class="modal" style="width:min(480px, 100%)">
        <div class="modal-head">
          <h2 class="title">내 여행 목록</h2>
          <button class="close" onclick="closeModal('tripModal')">×</button>
        </div>
        <div id="tripListContent"></div>
      </div>
    </div>

    <!-- 4. 퀘스트 상세 모달 -->
    <div class="modal-bg" id="detailModal" onclick="closeModalOutside(event, 'detailModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">퀘스트 상세</h2>
          <button class="close" onclick="closeModal('detailModal')">×</button>
        </div>
        <div id="detailContent"></div>
      </div>
    </div>

    <!-- 5. 퀘스트 생성 모달 -->
    <div class="modal-bg" id="createModal" onclick="closeModalOutside(event, 'createModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">✨ 나만의 퀘스트 만들기</h2>
          <button class="close" onclick="closeModal('createModal')">×</button>
        </div>
        <div style="margin-bottom:16px;">
          <label class="form-label">퀘스트 제목</label>
          <input id="newTitle" class="form-input" placeholder="예: 광안리 일몰 보기">
        </div>
        <button class="btn btn-primary" style="width:100%" onclick="createQuest()">퀘스트 발행하기</button>
      </div>
    </div>

    <!-- 컨텍스트 메뉴 및 토스트 -->
    <div class="context-menu" id="rowMenu"></div>
    <div class="toast desc" id="toast"></div>

    <!-- ==========================================
         전역 스크립트 및 렌더링 로직
    =========================================== -->
    <script>
        /* 데이터 정의 */
        const quests = [
            {id:1, title:'광안리에서 일몰 사진 남기기', cat:'사진', place:'부산 수영구 · 광안리', time:'40분', xp:120, rating:4.8, popular:1420, new:false, icon:'🌅'},
            {id:2, title:'부산 로컬 카페 하나 찾아보기', cat:'카페', place:'부산진구 · 전포동', time:'60분', xp:150, rating:4.7, popular:1180, new:false, icon:'☕'},
            {id:3, title:'해운대 30분 산책하기', cat:'산책', place:'부산 해운대구', time:'30분', xp:100, rating:4.6, popular:950, new:true, icon:'🚶'},
            {id:4, title:'부산에서 돼지국밥 먹어보기', cat:'맛집', place:'부산 전역', time:'60분', xp:180, rating:4.9, popular:890, new:false, icon:'🍲'}
        ];

        let saved = JSON.parse(localStorage.getItem('q_saved') || '[]');
        let planned = JSON.parse(localStorage.getItem('q_planned') || '[]');
        let done = JSON.parse(localStorage.getItem('q_done') || '[]');
        let xp = Number(localStorage.getItem('q_xp') || 0);

        const getQ = id => quests.find(q => q.id === id);

        function saveState() {
            localStorage.setItem('q_saved', JSON.stringify(saved));
            localStorage.setItem('q_planned', JSON.stringify(planned));
            localStorage.setItem('q_done', JSON.stringify(done));
            localStorage.setItem('q_xp', xp);
        }

        /* 탭 전환 */
        function switchTab(id, btn) {
            document.querySelectorAll('.tab-content').forEach(x => x.classList.remove('active'));
            document.getElementById('tab-' + id).classList.add('active');
            document.querySelectorAll('.main-tab-btn').forEach(x => x.classList.remove('active'));
            if(btn) btn.classList.add('active');
            if(id === 'management') renderManagement();
        }

        /* 토스트 함수 */
        function showToast(msg, icon = '✨') {
            const t = document.getElementById('toast');
            t.innerHTML = `<span>\${icon}</span> <span>\${msg}</span>`;
            t.classList.add('show');
            clearTimeout(window.toastTimer);
            window.toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
        }

        function closeModal(id) {
            const target = document.getElementById(id);
            if(target) target.classList.remove('show');
        }
        function closeModalOutside(e, id) {
            if(e.target.id === id) closeModal(id);
        }

        /* 탐색 탭 카드 템플릿 및 렌더링 */
        function cardTemplate(q) {
            const isSaved = saved.includes(q.id);
            const isPlanned = planned.includes(q.id);
            return `
                <div class="dash-card card" onclick="openDetailModal(\${q.id})">
                    <div class="thumb">\${q.icon}</div>
                    <div class="badges">
                        <span class="badge">\${q.cat}</span>
                        <span class="badge orange">+\${q.xp} XP</span>
                    </div>
                    <h3 class="subtitle" style="margin: 8px 0;">\${q.title}</h3>
                    <div class="desc">📍 \${q.place} · ⏱ \${q.time}</div>
                    <div class="card-bottom">
                        <span class="desc" style="color:var(--color-accent); font-weight:bold;">⭐ \${q.rating}</span>
                        <div class="card-actions">
                            <button class="small-btn" onclick="event.stopPropagation(); toggleSave(\${q.id})">
                                \${isSaved ? '♥' : '♡'}
                            </button>
                            <button class="small-btn" onclick="event.stopPropagation(); addSchedule(\${q.id})">
                                \${isPlanned ? '일정 ✓' : '+ 일정'}
                            </button>
                        </div>
                    </div>
                </div>`;
        }

        function renderQuestCards() {
            const popularGrid = document.getElementById('popularGrid');
            const newGrid = document.getElementById('newGrid');
            if(popularGrid) popularGrid.innerHTML = quests.map(cardTemplate).join('');
            if(newGrid) newGrid.innerHTML = quests.filter(x => x.new).map(cardTemplate).join('');
        }

        /* 저장/일정 추가 핸들러 */
        function toggleSave(id) {
            if(saved.includes(id)) {
                saved = saved.filter(x => x !== id);
                showToast('컬렉션에서 제외했습니다.', '🤍');
            } else {
                saved.push(id);
                showToast('♡ 내 컬렉션에 저장했습니다.');
            }
            saveState();
            renderQuestCards();
        }

        function addSchedule(id) {
            if(!planned.includes(id)) {
                planned.push(id);
                saveState();
                showToast('📅 여행 일정에 추가되었습니다.');
                renderQuestCards();
            } else {
                showToast('이미 일정에 추가된 퀘스트입니다.');
            }
        }

        /* 관리 탭 렌더링 */
        function renderManagement() {
            const list = document.getElementById('todayList');
            if(!list) return;

            if(!planned.length) {
                list.innerHTML = '<div class="desc" style="text-align:center; padding: 20px;">일정에 담긴 퀘스트가 없습니다.</div>';
            } else {
                list.innerHTML = planned.map(id => {
                    const q = getQ(id);
                    if(!q) return '';
                    const isDone = done.includes(id);
                    return `
                        <div class="quest-row">
                            <input type="checkbox" \${isDone ? 'checked' : ''} onchange="toggleDone(\${id})">
                            <div class="quest-info">
                                <strong class="subtitle">\${q.icon} \${q.title}</strong>
                                <div class="desc">📍 \${q.place} · +\${q.xp} XP</div>
                            </div>
                            <span class="status \${isDone ? 'done' : ''}">\${isDone ? '완료 ✓' : '예정'}</span>
                        </div>`;
                }).join('');
            }

            document.getElementById('totalCount').textContent = planned.length;
            document.getElementById('doneCount').textContent = done.length;
            document.getElementById('xpTotal').textContent = xp;

            const pct = planned.length ? (done.length / planned.length) * 100 : 0;
            document.getElementById('progressBar').style.width = pct + '%';
        }

        function toggleDone(id) {
            const q = getQ(id);
            if(done.includes(id)) {
                done = done.filter(x => x !== id);
                xp = Math.max(0, xp - q.xp);
            } else {
                done.push(id);
                xp += q.xp;
                showToast(`🎉 퀘스트 완료! +\${q.xp} XP 획득`);
            }
            saveState();
            renderManagement();
        }

        /* 모달 제어 함수들 */
        function openRegionModal() { document.getElementById('regionModal').classList.add('show'); }
        function applyRegions() { closeModal('regionModal'); showToast('부산 지역 필터 적용됨', '📍'); }

        function openCollectionModal() {
            const container = document.getElementById('collectionBody');
            const savedQs = saved.map(getQ).filter(Boolean);
            if(!savedQs.length) {
                container.innerHTML = '<div class="desc" style="text-align:center; padding:20px;">저장된 컬렉션이 없습니다.</div>';
            } else {
                container.innerHTML = savedQs.map(q => `
                    <div class="collection-row">
                        <div style="font-size:24px;">\${q.icon}</div>
                        <div style="flex:1;">
                            <div class="subtitle">\${q.title}</div>
                            <div class="desc">📍 \${q.place}</div>
                        </div>
                        <button class="small-btn" onclick="addSchedule(\${q.id})">+ 일정</button>
                    </div>
                `).join('');
            }
            document.getElementById('collectionModal').classList.add('show');
        }

        function openTripPickerModal() { document.getElementById('tripModal').classList.add('show'); }
        function openCreateModal() { document.getElementById('createModal').classList.add('show'); }

        function createQuest() {
            const titleInput = document.getElementById('newTitle');
            const title = titleInput.value.trim();
            if(!title) return showToast('제목을 입력해 주세요.');

            const newQ = { id: Date.now(), title: title, cat: '자작', place: '부산', time: '30분', xp: 100, rating: 5.0, popular: 0, new: true, icon: '✨' };
            quests.push(newQ);
            planned.push(newQ.id);
            saveState();

            titleInput.value = '';
            closeModal('createModal');
            showToast('✨ 나만의 퀘스트가 일정에 추가되었습니다.');
            renderQuestCards();
        }

        function openDetailModal(id) {
            const q = getQ(id);
            if(!q) return;
            document.getElementById('detailContent').innerHTML = `
                <div style="text-align:center; padding: 10px 0;">
                    <div style="font-size:48px;">\${q.icon}</div>
                    <h3 class="title" style="margin:10px 0;">\${q.title}</h3>
                    <p class="desc">\${q.place} · ⏱ \${q.time}</p>
                    <button class="btn btn-primary" onclick="addSchedule(\${q.id}); closeModal('detailModal');">일정에 추가하기</button>
                </div>
            `;
            document.getElementById('detailModal').classList.add('show');
        }

        /* 초기화 실행 */
        document.addEventListener('DOMContentLoaded', () => {
            renderQuestCards();
        });
    </script>
</body>
</html>