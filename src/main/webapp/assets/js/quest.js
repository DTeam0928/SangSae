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
                    <span class="badge">${App.escape(q.cat)}</span>
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

        if (popularGrid) popularGrid.innerHTML = quests.map(cardTemplate).join('');
        if (newGrid) newGrid.innerHTML = quests.map(cardTemplate).join('');
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

    function openRegionModal() { document.getElementById('regionModal')?.classList.add('show'); }
    function applyRegions() { closeModal('regionModal'); showToast('지역 필터 적용됨', '📍'); }

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
        if (searchTitle) searchTitle.textContent = term ? '“' + value.trim() + '” 검색 결과' : questCategory + ' 퀘스트';

        const filtered = quests.filter(q => (questCategory === '전체' || q.cat === questCategory) && [q.title, q.place, q.cat].join(' ').toLocaleLowerCase().includes(term));
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
        openRegionModal, applyRegions, openCollectionModal, openTripPickerModal,
        openDetailModal, handleSearch, clearSearch, setCategory, renderExplorerExtras,
        addCourse, switchCalendarMode, moveQuestCalendar, selectDate,
        initQuestMap, renderQuestMapMarkers, focusUserLocation, openCreateModal, 
		toggleCreatePlaceSearch, searchCreatePlaces, confirmAuthRadius
    };
})();
