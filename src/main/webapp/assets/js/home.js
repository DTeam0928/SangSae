(() => {
    'use strict';

    let initialized = false;
    let deleteTarget = null;
    let previousFocus = null;
    let xpFrame = null;

    // -------------------------------------------------------------
    // 1. API 데이터 페치 및 DOM 바인딩
    // -------------------------------------------------------------
    async function fetchHomeData() {
        const contextPath = document.body.dataset.contextPath || '';
        const apiUrl = `${contextPath}/api/home`;

        try {
            // api-client.js의 api.get 사용
            const res = await api.get(apiUrl);

            // API 반환 형태에 맞추어 데이터 추출 (res 또는 res.data)
            if (res && (res.success || res.status === 'success' || res.data)) {
                const data = res.data || res;
                renderDashboardData(data);
            }
        } catch (error) {
            console.error('홈 데이터를 불러오는 중 오류 발생:', error);
        }
    }

    function renderDashboardData(data) {
        const contextPath = document.body.dataset.contextPath || '';

        // 닉네임
        const nicknameEl = document.getElementById('user-nickname');
        if (nicknameEl && data.nickname) {
            nicknameEl.textContent = data.nickname;
        }

        // XP 및 오늘 달성도 (user_daily_aggregates 반영)
        const streakEl = document.getElementById('user-streak-badge');
        const earnedXpEl = document.getElementById('earned-xp-num');
        const routineStatusEl = document.getElementById('routine-status-text');
        const progressBar = document.getElementById('xp-progress');

        if (data.dailyAggregate) {
            const agg = data.dailyAggregate;
            const streak = agg.currentStreak ?? 0;
            const earnedXp = agg.earnedXp ?? 0;
            const successCount = agg.routineSuccessCount ?? 0;
            const totalCount = agg.routineTotalCount ?? 0;

            if (streakEl) streakEl.textContent = `🔥 연속 ${streak}일`;
            if (earnedXpEl) earnedXpEl.textContent = Number(earnedXp).toLocaleString();
            if (routineStatusEl) routineStatusEl.textContent = `${successCount} / ${totalCount}개`;

            const percent = totalCount > 0 ? Math.min(100, Math.round((successCount / totalCount) * 100)) : 0;
            if (progressBar) {
                progressBar.dataset.progress = percent;
                animateXpBar(percent);
            }
        }

        // 1. 여행 목록 렌더링
        const tripContent = document.getElementById('trip-content');
        if (tripContent && Array.isArray(data.trips) && data.trips.length > 0) {
            tripContent.innerHTML = data.trips.map((item, idx) => `
                <div class="item-row" data-item-row data-id="${item.id}">
                    <div class="item-left-group">
                        <span class="item-number">${idx + 1}</span>
                        <div>
                            <div class="subtitle">${escapeHtml(item.title)}</div>
                            <div class="desc item-subtext">${escapeHtml(item.startDate || '')} - ${escapeHtml(item.endDate || '')}</div>
                        </div>
                    </div>
                    <div class="item-btn-group">
                        <a href="${contextPath}/quest#management" class="small-btn">수정</a>
                        <button type="button" class="small-btn danger" data-action="open-delete" data-type="여행" data-name="${escapeHtml(item.title)}">삭제</button>
                    </div>
                </div>
            `).join('');
        }

        // 2. 루틴 목록 렌더링
        const routineContent = document.getElementById('routine-content');
        if (routineContent && Array.isArray(data.routines) && data.routines.length > 0) {
            routineContent.innerHTML = data.routines.map((item, idx) => `
                <div class="item-row" data-item-row data-id="${item.id}">
                    <div class="item-left-group">
                        <span class="item-number">${idx + 1}</span>
                        <div>
                            <div class="subtitle">${escapeHtml(item.title)}</div>
                            <div class="desc item-subtext">${escapeHtml(item.scheduleText || '매일 반복')}</div>
                        </div>
                    </div>
                    <div class="item-btn-group">
                        <a href="${contextPath}/routine#manage" class="small-btn">수정</a>
                        <button type="button" class="small-btn danger" data-action="open-delete" data-type="루틴" data-name="${escapeHtml(item.title)}">삭제</button>
                    </div>
                </div>
            `).join('');
        }

        // 3. 통계 데이터 렌더링
        if (data.stats) {
            const completedQuestsEl = document.getElementById('stat-completed-quests');
            const remainingQuestsEl = document.getElementById('stat-remaining-quests');
            const completedRoutinesEl = document.getElementById('stat-completed-routines');

            if (completedQuestsEl) completedQuestsEl.textContent = `${data.stats.completedQuests ?? 0}개`;
            if (remainingQuestsEl) remainingQuestsEl.textContent = `${data.stats.remainingQuests ?? 0}개`;
            if (completedRoutinesEl) completedRoutinesEl.textContent = `${data.stats.completedRoutines ?? 0}개`;
        }
    }

    function escapeHtml(str) {
        return String(str || '')
            .replace(/&/g, '&amp;')
            .replace(/</g, '&lt;')
            .replace(/>/g, '&gt;')
            .replace(/"/g, '&quot;')
            .replace(/'/g, '&#039;');
    }

    // -------------------------------------------------------------
    // 2. XP 프로그레스 바 애니메이션
    // -------------------------------------------------------------
    function animateXpBar(percent) {
        const progressBar = document.getElementById('xp-progress');
        const track = progressBar?.closest('[role="progressbar"]');

        if (!progressBar || !track) return;

        if (xpFrame !== null) {
            cancelAnimationFrame(xpFrame);
            xpFrame = null;
        }

        track.setAttribute('aria-valuenow', String(percent));
        progressBar.style.transition = 'none';
        progressBar.style.width = '0%';

        void progressBar.offsetWidth; // 리플로우 강제

        if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
            progressBar.style.width = `${percent}%`;
            progressBar.style.removeProperty('transition');
            return;
        }

        xpFrame = requestAnimationFrame(() => {
            progressBar.style.removeProperty('transition');
            progressBar.style.width = `${percent}%`;
            xpFrame = null;
        });
    }

    // -------------------------------------------------------------
    // 3. 모달 및 이벤트 제어
    // -------------------------------------------------------------
    function formatMessage(template, values) {
        return String(template || '').replace(
            /\{(type|name)\}/g,
            (_, key) => values[key] ?? ''
        );
    }

    function openDeleteModal(button) {
        const modal = document.getElementById('deleteModal');
        const row = button.closest('[data-item-row]');

        if (!modal || !row) return;

        deleteTarget = row;
        previousFocus = button;

        const values = {
            type: button.dataset.type || '항목',
            name: button.dataset.name || ''
        };

        const titleEl = document.getElementById('modalTitle');
        const descEl = document.getElementById('modalDesc');

        if (titleEl) {
            titleEl.textContent = formatMessage(modal.dataset.titleTemplate || '{type} 삭제', values);
        }
        if (descEl) {
            descEl.textContent = formatMessage(modal.dataset.descriptionTemplate || '"{name}" {type}을(를) 정말 삭제하시겠습니까?', values);
        }

        modal.classList.add('show');
        modal.setAttribute('aria-hidden', 'false');
        modal.querySelector('[data-action="cancel-delete"]')?.focus();
    }

    function closeDeleteModal() {
        const modal = document.getElementById('deleteModal');
        if (!modal) return;

        modal.classList.remove('show');
        modal.setAttribute('aria-hidden', 'true');
        deleteTarget = null;

        if (previousFocus?.isConnected) previousFocus.focus();
        previousFocus = null;
    }

    function confirmDelete() {
        if (!deleteTarget) {
            closeDeleteModal();
            return;
        }

        // 삭제 요청 API 연동 공간 (필요 시 api.post/delete 추가 가능)
        deleteTarget.remove();
        closeDeleteModal();
    }

    function handleModalKeyboard(event) {
        const modal = document.getElementById('deleteModal');
        if (!modal?.classList.contains('show')) return;

        if (event.key === 'Escape') {
            event.preventDefault();
            closeDeleteModal();
            return;
        }

        if (event.key !== 'Tab') return;

        const buttons = [...modal.querySelectorAll('button:not([disabled])')];
        const first = buttons[0];
        const last = buttons[buttons.length - 1];

        if (!first || !last) return;

        if (event.shiftKey && document.activeElement === first) {
            event.preventDefault();
            last.focus();
        } else if (!event.shiftKey && document.activeElement === last) {
            event.preventDefault();
            first.focus();
        }
    }

    function initHome() {
        if (initialized) return;
        initialized = true;

        const dashboard = document.getElementById('home-dashboard');
        const modal = document.getElementById('deleteModal');

        // 삭제 모달 호출 이벤트 (동적으로 추가된 삭제 버튼 처리)
        dashboard?.addEventListener('click', (event) => {
            const button = event.target.closest('[data-action="open-delete"]');
            if (button && dashboard.contains(button)) {
                openDeleteModal(button);
            }
        });

        modal?.addEventListener('click', (event) => {
            const action = event.target.closest('[data-action]')?.dataset.action;

            if (event.target === modal || action === 'cancel-delete') {
                closeDeleteModal();
            } else if (action === 'confirm-delete') {
                confirmDelete();
            }
        });

        document.addEventListener('keydown', handleModalKeyboard);

        // 초기 데이터 로드 (API)
        fetchHomeData();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initHome, { once: true });
    } else {
        initHome();
    }
})();