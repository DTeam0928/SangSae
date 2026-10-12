(() => {
'use strict';

    const contextPath = document.body.dataset.contextPath || '';

    function getCategoryIcon(cat) {
        if (!cat) return '✨';
        if (cat.includes('사진')) return '🌅';
        if (cat.includes('카페')) return '☕';
        if (cat.includes('산책') || cat.includes('걷기')) return '🚶';
        if (cat.includes('맛집') || cat.includes('음식')) return '🍲';
        if (cat.includes('문화') || cat.includes('예술')) return '🎨';
        if (cat.includes('액티비티') || cat.includes('운동')) return '🎢';
        return '✨';
    }

    // 순수 DB 데이터 저장소 (더미 데이터 전면 삭제)
    let quests = [];
    let serverUserQuests = [];
    let savedQuestIds = [];
    let currentXp = 0;
    let questMap = null;

    let calendarMode = 'week';
    let questCalendarCursor = new Date();
    let questCategory = '전체';
	let pendingScheduleQuestId = null;
	let selectedSido = '전국';
	let selectedSigungu = new Set();
	let appliedRegions = [];

	const KOREA_REGIONS = {
	    '서울특별시': ['종로구','중구','용산구','성동구','광진구','동대문구','중랑구','성북구','강북구','도봉구','노원구','은평구','서대문구','마포구','양천구','강서구','구로구','금천구','영등포구','동작구','관악구','서초구','강남구','송파구','강동구'],
	    '부산광역시': ['중구','서구','동구','영도구','부산진구','동래구','남구','북구','해운대구','사하구','금정구','강서구','연제구','수영구','사상구','기장군'],
	    '대구광역시': ['중구','동구','서구','남구','북구','수성구','달서구','달성군','군위군'],
	    '인천광역시': ['중구','동구','미추홀구','연수구','남동구','부평구','계양구','서구','강화군','옹진군'],
	    '광주광역시': ['동구','서구','남구','북구','광산구'],
	    '대전광역시': ['동구','중구','서구','유성구','대덕구'],
	    '울산광역시': ['중구','남구','동구','북구','울주군'],
	    '세종특별자치시': ['세종시'],
	    '경기도': ['수원시','용인시','고양시','화성시','성남시','부천시','남양주시','안산시','평택시','안양시','시흥시','파주시','김포시','의정부시','광주시','하남시','광명시','군포시','양주시','오산시','이천시','안성시','구리시','의왕시','포천시','양평군','여주시','동두천시','과천시','가평군','연천군'],
	    '강원특별자치도': ['춘천시','원주시','강릉시','동해시','태백시','속초시','삼척시','홍천군','횡성군','영월군','평창군','정선군','철원군','화천군','양구군','인제군','고성군','양양군'],
	    '충청북도': ['청주시','충주시','제천시','보은군','옥천군','영동군','증평군','진천군','괴산군','음성군','단양군'],
	    '충청남도': ['천안시','공주시','보령시','아산시','서산시','논산시','계룡시','당진시','금산군','부여군','서천군','청양군','홍성군','예산군','태안군'],
	    '전북특별자치도': ['전주시','군산시','익산시','정읍시','남원시','김제시','완주군','진안군','무주군','장수군','임실군','순창군','고창군','부안군'],
	    '전라남도': ['목포시','여수시','순천시','나주시','광양시','담양군','곡성군','구례군','고흥군','보성군','화순군','장흥군','강진군','해남군','영암군','무안군','함평군','영광군','장성군','완도군','진도군','신안군'],
	    '경상북도': ['포항시','경주시','김천시','안동시','구미시','영주시','영천시','상주시','문경시','경산시','의성군','청송군','영양군','영덕군','청도군','고령군','성주군','칠곡군','예천군','봉화군','울진군','울릉군'],
	    '경상남도': ['창원시','진주시','통영시','사천시','김해시','밀양시','거제시','양산시','의령군','함안군','창녕군','고성군','남해군','하동군','산청군','함양군','거창군','합천군'],
	    '제주특별자치도': ['제주시','서귀포시']
	};

    /* 탭 전환 */
    function switchTab(id, btn) {
        document.querySelectorAll('.tab-content').forEach(x => x.classList.remove('active'));
        const targetTab = document.getElementById('tab-' + id);
        if (targetTab) targetTab.classList.add('active');
        document.querySelectorAll('.main-tab-btn').forEach(x => x.classList.remove('active'));
        if (btn) btn.classList.add('active');
        if (id === 'management') {
            fetchUserQuestsFromServer();
        } else if (id === 'explorer' && questMap) {
            setTimeout(() => {
                questMap.relayout();
                renderQuestMapMarkers();
            }, 50);
        }
    }

    /* 토스트 알림 */
    function showToast(msg, icon = '✨') {
        const t = document.getElementById('toast');
        if (!t) return;
        t.textContent = icon + ' ' + msg;
        t.classList.add('show');
        clearTimeout(window.toastTimer);
        window.toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
    }
	
	function openCreateModal() {
	    document.getElementById('createModal')?.classList.add('show');
	}

	function toggleCreatePlaceSearch() {
	    const authType = document.getElementById('newAuthType')?.value;
	    const placeSection = document.getElementById('createPlaceSection');

	    if (!placeSection) return;

	    if (authType === 'LOCATION') {
	        placeSection.style.display = 'block';
	    } else {
	        placeSection.style.display = 'none';
			window.selectedCreatePlace = null;

	        // 다른 인증 방식으로 변경하면 기존 장소 선택 초기화
	        const query = document.getElementById('createPlaceQuery');
	        const results = document.getElementById('createPlaceResults');
	        const selected = document.getElementById('createSelectedPlace');

	        if (query) query.value = '';
	        if (results) results.innerHTML = '';
	        if (selected) selected.innerHTML = '';
	    }
	}
	
	async function searchCreatePlaces() {
	    const query = document.getElementById('createPlaceQuery')?.value.trim();
	    const results = document.getElementById('createPlaceResults');
	    if (!results) return;
	    if (!query) {
	        showToast('검색할 장소 이름을 입력해 주세요.');
	        return;
	    }
		window.selectedCreatePlace = null;
		document.getElementById('createSelectedPlace')?.replaceChildren();
	    results.textContent = '장소를 검색하고 있습니다...';
	    try {
	        const response = await fetch(
	            contextPath + '/api/map/search?query=' + encodeURIComponent(query),
	            { headers: { 'Accept': 'application/json' } }
	        );
	        const json = await response.json();
	        if (!response.ok || !json.success) {
	            throw new Error(json.message || '장소 검색에 실패했습니다.');
	        }
	        results.replaceChildren();
	        if (!json.data || json.data.length === 0) {
	            results.textContent = '검색 결과가 없습니다.';
	            return;
	        }
	        json.data.forEach(place => {
	            const button = document.createElement('button');
	            button.type = 'button';
	            button.className = 'btn';
	            button.style.display = 'block';
	            button.style.width = '100%';
	            button.style.marginTop = '8px';
	            button.style.textAlign = 'left';
	            const name = document.createElement('strong');
	            name.textContent = place.name;
	            const address = document.createElement('div');
	            address.textContent = place.roadAddress || place.address || '';
	            address.style.fontSize = '12px';
	            button.append(name, address);
	            button.addEventListener('click', () => {
	                window.selectedCreatePlace = place;
	                document.getElementById('createSelectedPlace').textContent =
	                    '📍 선택한 장소: ' + place.name;
	                results.replaceChildren();
	            });
	            results.appendChild(button);
	        });
	    } catch (error) {
	        console.error('장소 검색 오류:', error);
	        results.textContent = '장소 검색 중 오류가 발생했습니다.';
	        showToast(error.message);
	    }
	}
	
	async function createQuest() {
	    const title = document.getElementById('newTitle')?.value.trim();
	    const description = document.getElementById('newDescription')?.value.trim();
	    const category = document.getElementById('newCategory')?.value;
	    const estimatedMinutes = document.getElementById('newEstimatedMinutes')?.value;
	    const rewardXp = document.getElementById('newRewardXp')?.value;
	    const authType = document.getElementById('newAuthType')?.value;
	    const visibility = document.getElementById('newVisibility')?.value;

	    if (!title) {
	        showToast('퀘스트 제목을 입력해 주세요.', '⚠️');
	        return;
	    }

	    if (!estimatedMinutes || Number(estimatedMinutes) <= 0) {
	        showToast('예상 소요 시간을 확인해 주세요.', '⚠️');
	        return;
	    }

	    if (rewardXp === '' || Number(rewardXp) < 0) {
	        showToast('보상 XP를 확인해 주세요.', '⚠️');
	        return;
	    }

	    if (authType === 'LOCATION' && !window.selectedCreatePlace) {
	        showToast('위치 인증에 사용할 장소를 선택해 주세요.', '⚠️');
	        return;
	    }

	    const publishButton = document.querySelector('#createModal .quest-detail-4');

	    if (publishButton) {
	        publishButton.disabled = true;
	        publishButton.textContent = '발행 중...';
	    }

	    try {
	        let placeId = null;

	        // LOCATION 인증이면 장소를 먼저 DB에 등록
	        if (authType === 'LOCATION') {
	            const selectedPlace = window.selectedCreatePlace;
	            const query = document.getElementById('createPlaceQuery')?.value.trim();

	            const placeResponse = await fetch(contextPath + '/quests/place', {
	                method: 'POST',
	                headers: {
	                    'Content-Type': 'application/x-www-form-urlencoded',
	                    'Accept': 'application/json'
	                },
	                body: new URLSearchParams({
	                    kakaoPlaceId: selectedPlace.kakaoPlaceId,
	                    query: query
	                })
	            });

	            const placeJson = await placeResponse.json();

	            if (!placeResponse.ok || !placeJson.success) {
	                throw new Error(placeJson.message || '장소 등록에 실패했습니다.');
	            }

	            placeId = placeJson.data;
	        }

	        // 퀘스트 생성 요청
	        const params = new URLSearchParams({
	            title,
	            description: description || '',
	            category,
	            estimatedMinutes,
	            rewardXp,
	            authType,
	            visibility
	        });

	        if (placeId !== null) {
	            params.append('placeId', String(placeId));
	        }

	        const questResponse = await fetch(contextPath + '/quests/create', {
	            method: 'POST',
	            headers: {
	                'Content-Type': 'application/x-www-form-urlencoded',
	                'Accept': 'application/json'
	            },
	            body: params
	        });

	        const questJson = await questResponse.json();

	        if (!questResponse.ok || !questJson.success) {
	            throw new Error(questJson.message || '퀘스트 생성에 실패했습니다.');
	        }

	        showToast('퀘스트가 성공적으로 발행되었습니다!', '🎉');
	        closeModal('createModal');

	        // 생성 폼 초기화
	        [
	            'newTitle',
	            'newDescription',
	            'createPlaceQuery'
	        ].forEach(id => {
	            const element = document.getElementById(id);
	            if (element) element.value = '';
	        });

	        document.getElementById('createPlaceResults')?.replaceChildren();
	        document.getElementById('createSelectedPlace')?.replaceChildren();
	        window.selectedCreatePlace = null;

	        // 공개 퀘스트 목록 갱신
	        fetchQuestsFromServer();

	    } catch (error) {
	        console.error('퀘스트 생성 오류:', error);
	        showToast(error.message || '퀘스트 생성 중 오류가 발생했습니다.', '⚠️');

	    } finally {
	        if (publishButton) {
	            publishButton.disabled = false;
	            publishButton.textContent = '퀘스트 발행하기';
	        }
	    }
	}
	
    function closeModal(id) {
        if (id === 'regionModal' && typeof ModalManager !== 'undefined') return ModalManager.close(id);
        const target = document.getElementById(id);
        if (target) target.classList.remove('show');
    }
    function closeModalOutside(e, id) {
        if (e.target.id === id) closeModal(id);
    }

    /* 1. 서버 카탈로그 로드 (DB 조회만 허용) */
    function fetchQuestsFromServer() {
        fetch(contextPath + '/quest', { headers: { 'Accept': 'application/json' } })
            .then(res => res.ok ? res.json() : null)
            .then(json => {
                if (json && json.success && Array.isArray(json.data)) {
                    quests = json.data.map(item => {
                        const placeObj = item.place || null;
                        return {
                            id: item.id,
                            title: item.title,
							authType: item.authType,
                            cat: item.category || '일반',
                            place: placeObj ? (placeObj.roadAddress || placeObj.name) : '추천 장소',
                            placeName: placeObj ? placeObj.name : '',
                            latitude: placeObj ? placeObj.latitude : null,
                            longitude: placeObj ? placeObj.longitude : null,
                            themeCode: placeObj ? placeObj.themeCode : 'WALK',
                            time: (item.estimatedMinutes || 30) + '분',
                            xp: item.rewardXp || 500,
                            rating: 5.0,
                            popular: item.completionCount || 0,
                            icon: getCategoryIcon(item.category)
                        };
                    });
                } else {
                    quests = [];
                }
                renderQuestCards();
                renderRecommendedHero();
                renderQuestMapMarkers();
            })
            .catch(() => {
                quests = [];
                renderQuestCards();
            });
    }

    /* 2. 서버 사용자 일정 로드 (DB 조회만 허용) */
    function fetchUserQuestsFromServer() {
        const dateStr = App.dateKey(questCalendarCursor);
        fetch(contextPath + '/user-quests?date=' + dateStr, { headers: { 'Accept': 'application/json' } })
            .then(res => res.ok ? res.json() : null)
            .then(json => {
                if (json && json.success && Array.isArray(json.data)) {
                    serverUserQuests = json.data;
                } else {
                    serverUserQuests = [];
                }
                renderManagement();
            })
            .catch(() => {
                serverUserQuests = [];
                renderManagement();
            });
    }

    /* 상단 오늘의 추천 퀘스트 영역 동적 렌더링 */
    function renderRecommendedHero() {
        const heroContainer = document.getElementById('heroRecommendContainer');
        if (!heroContainer) return;

        if (!quests.length) {
            heroContainer.innerHTML = `
                <div class="feature-main" style="grid-column: 1 / -1; text-align: center; padding: 40px 20px;">
                    <h2 class="title" style="color: var(--color-muted);">🌟 등록된 추천 퀘스트가 없습니다</h2>
                    <p class="desc" style="margin-top: 8px;">DB에 퀘스트 템플릿을 등록하면 여기에 추천 퀘스트가 표시됩니다.</p>
                </div>
            `;
            return;
        }

        const topQ = quests[0];
        heroContainer.innerHTML = `
            <div class="feature-main">
                <div class="badges">
                    <span class="badge">오늘 추천</span>
                    <span class="badge orange">+${topQ.xp} XP</span>
                </div>
                <h2 class="quest-detail-5 title">${App.escape(topQ.title)}</h2>
                <p class="quest-detail-7 desc">${App.escape(topQ.place)}</p>
                <div class="desc">📍 ${App.escape(topQ.place)} · ⏱ ${App.escape(topQ.time)}</div>
                <div class="quest-detail-8">
                    <button class="quest-detail-6 btn btn-primary" onclick="App.quest.openDetailModal(${topQ.id})">퀘스트 보기</button>
                    <button class="btn btn-secondary" onclick="App.quest.toggleSave(${topQ.id})">♡</button>
                </div>
            </div>
            <div class="feature-side">
                <div class="quest-detail-9 dash-card">
                    <strong class="subtitle">🎯 분류</strong>
                    <span class="desc">${App.escape(topQ.cat)}</span>
                </div>
                <div class="quest-detail-9 dash-card">
                    <strong class="subtitle">🏆 완료 보상</strong>
                    <span class="quest-detail-10 desc">+${topQ.xp} XP (위치인증)</span>
                </div>
            </div>
        `;
    }

    /* 탐색 탭 카드 템플릿 */
    function cardTemplate(q) {
        const isSaved = savedQuestIds.includes(q.id);
        const isPlanned = serverUserQuests.some(uq => uq.originTemplateId === q.id);
        return `
            <div class="dash-card card" onclick="App.quest.openDetailModal(${q.id})">
                <div class="badges">
                    <span class="badge">${App.escape(getCategoryName(q.cat))}</span>
                    <span class="badge orange">+${q.xp} XP</span>
                </div>
                <h3 class="subtitle" style="margin: 8px 0;">${App.escape(q.title)}</h3>
                <div class="desc">📍 ${App.escape(q.place)} · ⏱ ${App.escape(q.time)}</div>
                <div class="card-bottom">
                    <span class="desc" style="color:var(--color-accent); font-weight:bold;">인증 완료 ${q.popular}회</span>
                    <div class="card-actions">
                        <button class="small-btn" onclick="event.stopPropagation(); App.quest.toggleSave(${q.id})">
                            ${isSaved ? '♥' : '♡'}
                        </button>
                        <button class="small-btn" onclick="event.stopPropagation(); App.quest.addSchedule(${q.id})">
                            ${isPlanned ? '담김 ✓' : '+ 일정'}
                        </button>
                    </div>
                </div>
            </div>`;
    }

    function renderQuestCards() {
        const popularGrid = document.getElementById('popularGrid');
        const newGrid = document.getElementById('newGrid');
        const badge = document.getElementById('plannedCountBadge');

        if (badge) badge.textContent = '(' + serverUserQuests.length + ')';

        if (!quests.length) {
            const emptyHtml = '<div class="desc" style="text-align:center; padding: 40px; grid-column: 1 / -1; width:100%;">DB에 등록된 퀘스트가 없습니다.</div>';
            if (popularGrid) popularGrid.innerHTML = emptyHtml;
            if (newGrid) newGrid.innerHTML = emptyHtml;
            return;
        }

        const regionQuests = filterQuestsByRegion(quests);
        if (popularGrid) popularGrid.innerHTML = regionQuests.map(cardTemplate).join('');
        if (newGrid) newGrid.innerHTML = regionQuests.map(cardTemplate).join('');
        renderExplorerExtras();
    }

    function toggleSave(id) {
        if (savedQuestIds.includes(id)) {
            savedQuestIds = savedQuestIds.filter(x => x !== id);
            showToast('컬렉션에서 제외했습니다.', '🤍');
        } else {
            savedQuestIds.push(id);
            showToast('♡ 내 컬렉션에 저장했습니다.');
        }
        renderQuestCards();
    }

    /* 일정 추가 (오직 백엔드 DB 통신만 수행) */
	function addSchedule(id, authRadiusM = null) {
	    const quest = quests.find(q => q.id === id);

	    // LOCATION 퀘스트이며 아직 반경을 선택하지 않았다면 모달 표시
	    if (quest && quest.authType === 'LOCATION' && authRadiusM === null) {
	        pendingScheduleQuestId = id;
	        document.getElementById('authRadiusModal')?.classList.add('show');
	        return;
	    }
	    const dateStr = App.dateKey(questCalendarCursor);
	    const params = new URLSearchParams({
	        templateId: String(id),
	        scheduledDate: dateStr
	    });

	    // 인증 반경을 선택한 경우에만 전송
	    if (authRadiusM !== null) {
	        params.append('authRadiusM', String(authRadiusM));
	    }

	    fetch(contextPath + '/quest', {
	        method: 'POST',
	        headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
	        body: params
	    })
	    .then(res => res.json())
	    .then(json => {
	        if (json && json.success) {
	            showToast('📅 DB 일정에 추가되었습니다!');
	            fetchUserQuestsFromServer();
	        } else {
	            showToast(json.message || '일정 추가에 실패했습니다.', '⚠️');
	        }
	    })
	    .catch(() => {
	        showToast('일정 추가 중 통신 오류가 발생했습니다.', '⚠️');
	    });
	}

	function confirmAuthRadius() {
	    if (pendingScheduleQuestId === null) return;

	    const radius = Number(document.getElementById('authRadiusM')?.value);

	    if (!Number.isInteger(radius) || radius < 100 || radius > 1000) {
	        showToast('인증 반경은 100~1000m 사이여야 합니다.', '⚠️');
	        return;
	    }

	    const questId = pendingScheduleQuestId;
	    pendingScheduleQuestId = null;

	    closeModal('authRadiusModal');
	    addSchedule(questId, radius);
	}
	
    /* 관리 탭 렌더링 (순수 DB 데이터만 출력) */
    function renderManagement() {
        const list = document.getElementById('todayList');
        const totalElem = document.getElementById('totalCount');
        const doneElem = document.getElementById('doneCount');
        const xpElem = document.getElementById('xpTotal');
        const barElem = document.getElementById('progressBar');
        const badge = document.getElementById('plannedCountBadge');

        if (!list) return;

        const total = serverUserQuests.length;
        const doneCount = serverUserQuests.filter(x => x.status === 'COMPLETED').length;

        if (totalElem) totalElem.textContent = String(total);
        if (doneElem) doneElem.textContent = String(doneCount);
        if (badge) badge.textContent = '(' + total + ')';

        const pct = total ? (doneCount / total) * 100 : 0;
        if (barElem) barElem.style.width = pct + '%';

        if (!serverUserQuests.length) {
            list.innerHTML = '<div class="desc" style="text-align:center; padding: 30px;">일정에 담긴 퀘스트가 없습니다.<br>퀘스트 탐색 탭에서 퀘스트를 추가해 보세요!</div>';
            return;
        }

        list.innerHTML = serverUserQuests.map(item => {
            const isCompleted = item.status === 'COMPLETED';
            return `
                <div class="quest-row" style="display:flex; align-items:center; justify-content:space-between; gap:12px; padding:14px; border-bottom:1px solid var(--color-border, #eee);">
                    <input type="checkbox" ${isCompleted ? 'checked disabled' : ''} onchange="App.quest.completeQuestAction(${item.id})">
                    <div class="quest-info" style="flex:1;">
                        <strong class="subtitle">${App.escape(item.title)}</strong>
                        <div class="desc">📍 인증반경 ${item.authRadiusM || 100}m · 보상 +${item.rewardXp || 500} XP</div>
                    </div>
                    <span class="status ${isCompleted ? 'done' : ''}">${isCompleted ? '완료 ✓' : '미완료'}</span>
                    <button type="button" class="small-btn" style="color:var(--color-danger, #e53e3e); border:none; background:transparent; cursor:pointer;" onclick="App.quest.deleteScheduleItem(${item.id})">✕</button>
                </div>`;
        }).join('');
    }

    /* 퀘스트 GPS 인증 완료 (백엔드 DB 연동) */
    function completeQuestAction(userQuestId) {
        showToast('📍 GPS 위치를 확인하고 있습니다…', '🧭');
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(
                pos => sendCompleteQuest(userQuestId, pos.coords.latitude, pos.coords.longitude),
                () => sendCompleteQuest(userQuestId, null, null),
                { enableHighAccuracy: true, timeout: 6000, maximumAge: 0 }
            );
        } else {
            sendCompleteQuest(userQuestId, null, null);
        }
    }

    function sendCompleteQuest(userQuestId, lat, lng) {
        const params = new URLSearchParams({ userQuestId: String(userQuestId) });
        if (lat != null) params.append('latitude', String(lat));
        if (lng != null) params.append('longitude', String(lng));

        fetch(contextPath + '/user-quests/complete', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: params
        })
        .then(res => res.json())
        .then(json => {
            if (json && json.success) {
                const reward = json.data && json.data.reward;
                const earned = (json.data && json.data.earnedXp) || 500;
                currentXp += earned;
                const xpElem = document.getElementById('xpTotal');
                if (xpElem) xpElem.textContent = String(currentXp);

                if (reward && reward.isLevelUp) {
                    showToast(`🎉 레벨업! [Lv.${reward.currentLevel.level} ${reward.currentLevel.title}] 달성! (+${earned} XP)`, '🏆');
                } else {
                    showToast(`🎉 퀘스트 완료! +${earned} XP 획득`);
                }
                // 완료된 퀘스트는 DB에서 삭제되므로 서버 일정 목록 즉시 재조회
                fetchUserQuestsFromServer();
            } else {
                showToast(json.message || '위치 인증에 실패했습니다.', '⚠️');
                fetchUserQuestsFromServer();
            }
        })
        .catch(() => {
            showToast('완료 처리 중 통신 오류가 발생했습니다.', '⚠️');
            fetchUserQuestsFromServer();
        });
    }

    /* 일정에서 삭제 (오직 백엔드 DB 삭제 요청만 수행) */
    function deleteScheduleItem(userQuestId) {
        if (!confirm('이 퀘스트를 여행 일정에서 삭제하시겠습니까?')) return;
        fetch(contextPath + '/user-quests', {
            method: 'POST',
            headers: { 'Content-Type': 'application/x-www-form-urlencoded', 'Accept': 'application/json' },
            body: new URLSearchParams({ userQuestId: String(userQuestId) })
        })
        .then(res => res.json())
        .then(json => {
            if (json && json.success) {
                showToast('일정에서 삭제했습니다.', '🗑️');
                fetchUserQuestsFromServer();
            } else {
                showToast(json.message || '삭제에 실패했습니다.', '⚠️');
            }
        })
        .catch(() => {
            showToast('일정 삭제 요청 중 오류가 발생했습니다.', '⚠️');
        });
    }

    function renderRegionPicker() {
        const sidoList = document.getElementById('regionSidoList');
        const sigunguList = document.getElementById('regionSigunguList');
        if (!sidoList || !sigunguList) return;
        const sidoNames = Object.keys(KOREA_REGIONS);
        sidoList.innerHTML = ['전국', ...sidoNames].map(name => '<button type="button" role="option" aria-selected="' + (selectedSido === name) + '" class="small-btn" style="display:block;width:100%;margin:4px 0;' + (selectedSido === name ? 'font-weight:700;outline:2px solid var(--color-primary,#4f46e5);' : '') + '" onclick="App.quest.selectRegionSido(' + JSON.stringify(name) + ')">' + name + '</button>').join('');
        if (selectedSido === '전국') {
            sigunguList.innerHTML = '<p class="desc">전국을 선택하면 지역 제한 없이 탐색합니다.</p>';
        } else {
            const districts = KOREA_REGIONS[selectedSido] || [];
            sigunguList.innerHTML = '<label style="display:block;margin:6px 0;"><input type="checkbox" ' + (selectedSigungu.size === districts.length ? 'checked' : '') + ' onchange="App.quest.toggleAllSigungu(this.checked)"> 전체 선택</label>' + districts.map(name => '<label style="display:inline-flex;align-items:center;gap:5px;min-width:110px;margin:5px 8px 5px 0;"><input type="checkbox" value="' + name + '" ' + (selectedSigungu.has(name) ? 'checked' : '') + ' onchange="App.quest.toggleSigungu(this.value,this.checked)">' + name + '</label>').join('');
        }
        const summary = document.getElementById('selectedRegionSummary');
        if (summary) summary.textContent = selectedSido === '전국' ? '현재 선택: 전국' : '현재 선택: ' + selectedSido + (selectedSigungu.size ? ' · ' + [...selectedSigungu].join(', ') : ' · 시·군·구를 선택해 주세요');
    }
    function openRegionModal() {
        selectedSido = appliedRegions.length ? appliedRegions[0].sido : '전국';
        selectedSigungu = new Set(appliedRegions.flatMap(region => region.sigungu));
        renderRegionPicker();
        if (typeof ModalManager !== 'undefined') ModalManager.open('regionModal');
        else document.getElementById('regionModal')?.classList.add('show');
    }
    function selectRegionSido(sido) {
        selectedSido = sido;
        selectedSigungu = new Set();
        renderRegionPicker();
    }
    function toggleSigungu(name, checked) {
        if (checked) selectedSigungu.add(name); else selectedSigungu.delete(name);
        renderRegionPicker();
    }
    function toggleAllSigungu(checked) {
        selectedSigungu = new Set(checked ? (KOREA_REGIONS[selectedSido] || []) : []);
        renderRegionPicker();
    }
    function resetRegions() {
        selectedSido = '전국';
        selectedSigungu.clear();
        renderRegionPicker();
    }
    function applyRegions() {
        if (selectedSido !== '전국' && selectedSigungu.size === 0) {
            showToast('적용할 시·군·구를 선택해 주세요.', '⚠️');
            return;
        }
        appliedRegions = selectedSido === '전국' ? [] : [{ sido: selectedSido, sigungu: [...selectedSigungu] }];
        if (typeof ModalManager !== 'undefined') ModalManager.close('regionModal'); else closeModal('regionModal');
        const label = appliedRegions.length ? appliedRegions[0].sido + ' ' + appliedRegions[0].sigungu.join(', ') : '전국';
        showToast(label + ' 지역 선택 적용됨', '📍');
        renderQuestCards();
        handleSearch(document.getElementById('searchInput')?.value || '');
    }

    function filterQuestsByRegion(items) {
        if (!appliedRegions.length) return items;
        return items.filter(q => appliedRegions.some(region => {
            const placeText = String(q.place || '').replace(/\\s+/g, '');
            const sidoPrefix = region.sido.replace(/특별자치도|특별시|광역시|자치시|도$/g, '');
            return (placeText.includes(sidoPrefix) || placeText.includes(region.sido)) &&
                region.sigungu.some(district => placeText.includes(district));
        }));
    }

    function openCollectionModal() {
        const container = document.getElementById('collectionBody');
        const savedList = quests.filter(q => savedQuestIds.includes(q.id));
        if (!container) return;
        if (!savedList.length) {
            container.innerHTML = '<div class="desc" style="text-align:center; padding:20px;">저장된 컬렉션이 없습니다.</div>';
        } else {
            container.innerHTML = savedList.map(q => `
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
        document.getElementById('collectionModal')?.classList.add('show');
    }

    function openTripPickerModal() {
        document.getElementById('tripListContent').innerHTML = '<button type="button" class="btn btn-secondary" onclick="App.quest.closeModal(\'tripModal\')">오늘의 일정 (기본)</button>';
        document.getElementById('tripModal')?.classList.add('show');
    }

    function openDetailModal(id) {
        const q = quests.find(item => item.id === id);
        if (!q) return;
        document.getElementById('detailContent').innerHTML = `
            <div style="text-align:center; padding: 10px 0;">
                <div style="font-size:48px;">${App.escape(q.icon)}</div>
                <h3 class="title" style="margin:10px 0;">${App.escape(q.title)}</h3>
                <p class="desc">${App.escape(q.place)} · ⏱ ${App.escape(q.time)}</p>
                <div class="desc" style="margin:10px 0; color:var(--color-primary);">완료 보상: +${q.xp} XP</div>
                <button class="btn btn-primary" onclick="App.quest.addSchedule(${q.id}); App.quest.closeModal('detailModal');">일정에 추가하기</button>
            </div>
        `;
        document.getElementById('detailModal')?.classList.add('show');
    }

    function handleSearch(value) {
        const term = value.trim().toLocaleLowerCase();
        const clearBtn = document.getElementById('clearSearchBtn');
        if (clearBtn) clearBtn.style.display = term ? 'flex' : 'none';
        const searchResults = document.getElementById('searchResults');
        if (searchResults) searchResults.classList.toggle('active', !!term || questCategory !== '전체');
        const exploreHome = document.getElementById('exploreHome');
        if (exploreHome) exploreHome.classList.toggle('is-hidden', !!term);
        const searchTitle = document.getElementById('searchResultTitle');
        if (searchTitle) searchTitle.textContent = term ? '“' + value.trim() + '” 검색 결과' : getCategoryName(questCategory) + ' 퀘스트';

        const filtered = filterQuestsByRegion(quests).filter(q =>
            (questCategory === '전체' || q.cat === questCategory) &&
            [q.title, q.place, q.cat].join(' ').toLocaleLowerCase().includes(term)
        );
        const searchGrid = document.getElementById('searchGrid');
        if (searchGrid) {
            searchGrid.innerHTML = filtered.length ? filtered.map(cardTemplate).join('') : '<p class="empty-state">검색 결과가 없습니다.</p>';
        }
    }
    function clearSearch() { const inp = document.getElementById('searchInput'); if (inp) inp.value = ''; handleSearch(''); }
    function setCategory(button, category) {
        questCategory = category;
        document.querySelectorAll('.quick .chip').forEach(x => x.classList.toggle('active', x === button));
        handleSearch(document.getElementById('searchInput')?.value || '');
    }
    function renderExplorerExtras() {
        const mapList = document.getElementById('mapList');
        if (mapList) {
            mapList.innerHTML = quests.length
                ? quests.map(q => '<button type="button" class="map-item" onclick="App.quest.openDetailModal(' + q.id + ')">' + App.escape(q.icon + ' ' + q.title) + '</button>').join('')
                : '<div class="desc" style="padding:10px;">등록된 퀘스트가 없습니다.</div>';
        }
        const courseGrid = document.getElementById('courseGrid');
        if (courseGrid) {
            courseGrid.innerHTML = quests.length
                ? '<button type="button" class="btn btn-secondary" onclick="App.quest.addCourse()">모든 공개 퀘스트 담기</button>'
                : '<div class="desc">등록된 코스가 없습니다.</div>';
        }
    }
    function addCourse() { quests.forEach(q => addSchedule(q.id)); }

    function switchCalendarMode(mode) {
        calendarMode = mode;
        document.getElementById('weekToggleBtn')?.classList.toggle('active', mode === 'week');
        document.getElementById('monthToggleBtn')?.classList.toggle('active', mode === 'month');
        const first = new Date(questCalendarCursor);
        first.setHours(0, 0, 0, 0);
        if (mode === 'month') first.setDate(1);
        first.setDate(first.getDate() - first.getDay());
        const count = mode === 'week' ? 7 : 42;
        const period = document.getElementById('periodLabel');
        if (period) period.textContent = questCalendarCursor.getFullYear() + '년 ' + (questCalendarCursor.getMonth() + 1) + '월';
        const calBody = document.getElementById('calendarBody');
        if (calBody) {
            calBody.innerHTML = '<div class="week-grid">' + Array.from({ length: count }, (_, i) => {
                const d = new Date(first);
                d.setDate(first.getDate() + i);
                const isSelected = App.dateKey(d) === App.dateKey(questCalendarCursor);
                return '<button type="button" class="day ' + (isSelected ? 'selected' : '') + '" onclick="App.quest.selectDate(\'' + App.dateKey(d) + '\')">' + d.getDate() + '</button>';
            }).join('') + '</div>';
        }
    }
    function selectDate(dateStr) {
        questCalendarCursor = new Date(dateStr + 'T00:00:00');
        switchCalendarMode(calendarMode);
        fetchUserQuestsFromServer();
    }
    function moveQuestCalendar(direction) {
        if (calendarMode === 'week') questCalendarCursor.setDate(questCalendarCursor.getDate() + direction * 7);
        else { questCalendarCursor.setDate(1); questCalendarCursor.setMonth(questCalendarCursor.getMonth() + direction); }
        switchCalendarMode(calendarMode);
        fetchUserQuestsFromServer();
    }

    function initQuestMap() {
        if (typeof MapUtil === 'undefined' || typeof kakao === 'undefined' || !kakao.maps) {
            return;
        }
        const container = document.getElementById('questMapContainer');
        if (!container) return;

        questMap = MapUtil.init('questMapContainer', {
            lat: 35.1587,
            lng: 129.1604,
            level: 6,
            showControls: true
        });

        renderQuestMapMarkers();
    }

    function renderQuestMapMarkers() {
        if (!questMap || typeof MapUtil === 'undefined') return;

        const markersData = quests
            .filter(q => q.latitude && q.longitude)
            .map(q => ({
                placeId: q.id,
                name: q.title,
                latitude: q.latitude,
                longitude: q.longitude,
                roadAddress: q.place,
                themeName: q.cat,
                themeCode: q.themeCode || 'WALK'
            }));

        const badge = document.getElementById('questMapBadge');
        if (badge) {
            badge.textContent = markersData.length
                ? ('부산 · 주변 ' + markersData.length + '개')
                : '부산 · 주변 퀘스트';
        }

        if (markersData.length > 0) {
            MapUtil.renderPlaceMarkers(questMap, markersData, {
                clearPrevious: true,
                onSelect: (item) => {
                    openDetailModal(item.placeId);
                }
            });
        }
    }

	function getCategoryName(code) {
	    const names = {
	        PARK: '산책',
	        CAFE: '카페',
	        CULTURE: '문화',
	        GYM: '운동',
	        EXPLORE: '탐방'
	    };
	    return names[code] || code;
	}
	
    function focusUserLocation() {
        if (!questMap || typeof MapUtil === 'undefined') {
            showToast('지도가 아직 준비되지 않았습니다.', '⚠️');
            return;
        }
        showToast('📍 현재 위치로 이동합니다…', '🧭');
        MapUtil.moveToCurrentLocation(
            questMap,
            () => showToast('현재 위치로 이동했습니다.', '📍'),
            () => showToast('현재 위치를 가져올 수 없습니다.', '⚠️')
        );
    }

    document.addEventListener('keydown', e => {
        if (e.key === 'Escape') document.querySelectorAll('.modal-bg.show').forEach(el => {
            if (['regionModal','collectionModal','tripModal','detailModal','createModal'].includes(el.id)) closeModal(el.id);
        });
    });

    document.addEventListener('DOMContentLoaded', () => {
        fetchQuestsFromServer();
        fetchUserQuestsFromServer();
        switchCalendarMode('week');
        initQuestMap();
        if (location.hash === '#management') {
            switchTab('management', document.querySelectorAll('.main-tab-btn')[1]);
        }
    });

    App.quest = {
        createQuest, switchTab, showToast, closeModal, closeModalOutside, cardTemplate, renderQuestCards,
        toggleSave, addSchedule, completeQuestAction, deleteScheduleItem,
        openRegionModal, applyRegions, selectRegionSido, toggleSigungu, toggleAllSigungu, resetRegions, openCollectionModal, openTripPickerModal,
        openDetailModal, handleSearch, clearSearch, setCategory, renderExplorerExtras,
        addCourse, switchCalendarMode, moveQuestCalendar, selectDate,
        initQuestMap, renderQuestMapMarkers, focusUserLocation, openCreateModal, 
		toggleCreatePlaceSearch, searchCreatePlaces, confirmAuthRadius
    };
})();
