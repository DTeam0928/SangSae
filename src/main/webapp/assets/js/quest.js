(() => {
'use strict';

        /* 데이터 정의 */
        const defaultQuests = [
            {id:1, title:'광안리에서 일몰 사진 남기기', cat:'사진', place:'부산 수영구 · 광안리', time:'40분', xp:120, rating:4.8, popular:1420, new:false, icon:'🌅'},
            {id:2, title:'부산 로컬 카페 하나 찾아보기', cat:'카페', place:'부산진구 · 전포동', time:'60분', xp:150, rating:4.7, popular:1180, new:false, icon:'☕'},
            {id:3, title:'해운대 30분 산책하기', cat:'산책', place:'부산 해운대구', time:'30분', xp:100, rating:4.6, popular:950, new:true, icon:'🚶'},
            {id:4, title:'부산에서 돼지국밥 먹어보기', cat:'맛집', place:'부산 전역', time:'60분', xp:180, rating:4.9, popular:890, new:false, icon:'🍲'}
        ];

        const quests = defaultQuests.concat(App.storage.read('q_custom_quests', []).filter(q => q && Number.isSafeInteger(q.id) && typeof q.title === 'string' && Number.isFinite(q.xp)));
        let saved = App.storage.read('q_saved', []);
        let planned = App.storage.read('q_planned', []);
        let done = App.storage.read('q_done', []);
        let xp = Number(App.storage.raw('q_xp') || 0);

        const getQ = id => quests.find(q => q.id === id);
        saved = [...new Set(saved)].filter(getQ);
        planned = [...new Set(planned)].filter(getQ);
        done = [...new Set(done)].filter(id => planned.includes(id));
        xp = Number.isFinite(xp) ? Math.max(0, xp) : 0;

        function saveState() {
            App.storage.set('q_custom_quests', JSON.stringify(quests.filter(q => !defaultQuests.some(d => d.id === q.id))));
            App.storage.set('q_saved', JSON.stringify(saved));
            App.storage.set('q_planned', JSON.stringify(planned));
            App.storage.set('q_done', JSON.stringify(done));
            App.storage.set('q_xp', xp);
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
            t.textContent = icon + ' ' + msg;
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
                <div class="dash-card card" onclick="App.quest.openDetailModal(${q.id})">
                    <div class="thumb">${App.escape(q.icon)}</div>
                    <div class="badges">
                        <span class="badge">${App.escape(q.cat)}</span>
                        <span class="badge orange">+${q.xp} XP</span>
                    </div>
                    <h3 class="subtitle" style="margin: 8px 0;">${App.escape(q.title)}</h3>
                    <div class="desc">📍 ${App.escape(q.place)} · ⏱ ${App.escape(q.time)}</div>
                    <div class="card-bottom">
                        <span class="desc" style="color:var(--color-accent); font-weight:bold;">⭐ ${q.rating}</span>
                        <div class="card-actions">
                            <button class="small-btn" onclick="event.stopPropagation(); App.quest.toggleSave(${q.id})">
                                ${isSaved ? '♥' : '♡'}
                            </button>
                            <button class="small-btn" onclick="event.stopPropagation(); App.quest.addSchedule(${q.id})">
                                ${isPlanned ? '일정 ✓' : '+ 일정'}
                            </button>
                        </div>
                    </div>
                </div>`;
        }

        function renderQuestCards() {
            const popularGrid = document.getElementById('popularGrid');
            const newGrid = document.getElementById('newGrid');
            if(popularGrid) popularGrid.innerHTML = [...quests].sort((a,b) => b.popular-a.popular).map(cardTemplate).join('');
            renderManagement();
            renderExplorerExtras();
            document.getElementById('plannedCountBadge').textContent = '(' + planned.length + ')';
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
                            <input type="checkbox" ${isDone ? 'checked' : ''} onchange="App.quest.toggleDone(${id})">
                            <div class="quest-info">
                                <strong class="subtitle">${App.escape(q.icon)} ${App.escape(q.title)}</strong>
                                <div class="desc">📍 ${App.escape(q.place)} · +${q.xp} XP</div>
                            </div>
                            <span class="status ${isDone ? 'done' : ''}">${isDone ? '완료 ✓' : '예정'}</span>
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
            if (!q || !planned.includes(id)) return;
            if(done.includes(id)) {
                done = done.filter(x => x !== id);
                xp = Math.max(0, xp - q.xp);
            } else {
                done.push(id);
                xp += q.xp;
                showToast(`🎉 퀘스트 완료! +${q.xp} XP 획득`);
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
                        <div style="font-size:24px;">${App.escape(q.icon)}</div>
                        <div style="flex:1;">
                            <div class="subtitle">${App.escape(q.title)}</div>
                            <div class="desc">📍 ${App.escape(q.place)}</div>
                        </div>
                        <button class="small-btn" onclick="App.quest.addSchedule(${q.id})">+ 일정</button>
                    </div>
                `).join('');
            }
            document.getElementById('collectionModal').classList.add('show');
        }

        function openTripPickerModal() { document.getElementById('tripListContent').innerHTML='<button type="button" class="btn btn-secondary" onclick="App.quest.closeModal(\'tripModal\')">부산 여행 · 현재 여행</button>'; document.getElementById('tripModal').classList.add('show'); }
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
                    <div style="font-size:48px;">${App.escape(q.icon)}</div>
                    <h3 class="title" style="margin:10px 0;">${App.escape(q.title)}</h3>
                    <p class="desc">${App.escape(q.place)} · ⏱ ${App.escape(q.time)}</p>
                    <button class="btn btn-primary" onclick="App.quest.addSchedule(${q.id}); App.quest.closeModal('detailModal');">일정에 추가하기</button>
                </div>
            `;
            document.getElementById('detailModal').classList.add('show');
        }

        /* 초기화 실행 */
        document.addEventListener('DOMContentLoaded', () => {
            renderQuestCards();
        });
    
let questCategory = '전체';
function handleSearch(value) {
    const term = value.trim().toLocaleLowerCase();
    document.getElementById('clearSearchBtn').style.display = term ? 'flex' : 'none';
    document.getElementById('searchResults').classList.toggle('active', !!term || questCategory !== '전체');
    document.getElementById('exploreHome').classList.toggle('is-hidden', !!term);
    document.getElementById('searchResultTitle').textContent = term ? '“' + value.trim() + '” 검색 결과' : questCategory + ' 퀘스트';
    document.getElementById('searchGrid').innerHTML = quests.filter(q => (questCategory === '전체' || q.cat === questCategory) && [q.title,q.place,q.cat].join(' ').toLocaleLowerCase().includes(term)).map(cardTemplate).join('') || '<p class="empty-state">검색 결과가 없습니다.</p>';
}
function clearSearch() { document.getElementById('searchInput').value=''; handleSearch(''); }
function setCategory(button, category) { questCategory=category; document.querySelectorAll('.quick .chip').forEach(x=>x.classList.toggle('active',x===button)); handleSearch(document.getElementById('searchInput').value); }
function renderExplorerExtras() {
    document.getElementById('mapList').innerHTML=quests.map(q=>'<button type="button" class="map-item" onclick="App.quest.openDetailModal('+q.id+')">'+App.escape(q.icon+' '+q.title)+'</button>').join('');
    document.getElementById('courseGrid').innerHTML='<button type="button" class="btn btn-secondary" onclick="App.quest.addCourse()">부산 하루 코스 · 모든 기본 퀘스트 담기</button>';
    handleSearch(document.getElementById('searchInput').value);
}
function addCourse() { defaultQuests.forEach(q=>addSchedule(q.id)); }
let calendarMode='week', questCalendarCursor=new Date();
function switchCalendarMode(mode) {
    calendarMode=mode;
    document.getElementById('weekToggleBtn').classList.toggle('active',mode==='week');
    document.getElementById('monthToggleBtn').classList.toggle('active',mode==='month');
    const first=new Date(questCalendarCursor); first.setHours(0,0,0,0);
    if(mode==='month') first.setDate(1);
    first.setDate(first.getDate()-first.getDay());
    const count=mode==='week'?7:42;
    document.getElementById('periodLabel').textContent=questCalendarCursor.getFullYear()+'년 '+(questCalendarCursor.getMonth()+1)+'월';
    document.getElementById('calendarBody').innerHTML='<div class="week-grid">'+Array.from({length:count},(_,i)=>{const d=new Date(first);d.setDate(first.getDate()+i);return '<button type="button" class="day '+(App.dateKey(d)===App.dateKey()?'selected':'')+'">'+d.getDate()+'</button>';}).join('')+'</div>';
}
function moveQuestCalendar(direction) { if(calendarMode==='week') questCalendarCursor.setDate(questCalendarCursor.getDate()+direction*7); else {questCalendarCursor.setDate(1);questCalendarCursor.setMonth(questCalendarCursor.getMonth()+direction);} switchCalendarMode(calendarMode); }
document.addEventListener('DOMContentLoaded',()=>{switchCalendarMode('week'); if(location.hash==='#management') switchTab('management',document.querySelectorAll('.main-tab-btn')[1]);});

App.quest = {saveState, switchTab, showToast, closeModal, closeModalOutside, cardTemplate, renderQuestCards, toggleSave, addSchedule, renderManagement, toggleDone, openRegionModal, applyRegions, openCollectionModal, openTripPickerModal, openCreateModal, createQuest, openDetailModal, handleSearch, clearSearch, setCategory, renderExplorerExtras, addCourse, switchCalendarMode, moveQuestCalendar};
})();
