(() => {
    'use strict';

    let initialized = false;
    let deleteTarget = null;
    let previousFocus = null;
    let xpFrame = null;

    function formatMessage(template, values) {
        return String(template || '').replace(
            /\{(type|name)\}/g,
            (_, key) => values[key] ?? ''
        );
    }

    function localDateKey() {
        const now = new Date();
        const year = now.getFullYear();
        const month = String(now.getMonth() + 1).padStart(2, '0');
        const day = String(now.getDate()).padStart(2, '0');

        return `${year}-${month}-${day}`;
    }

    function getItemStatus(row, type, today) {
        if (type === 'routine') {
            return row.dataset.status || '';
        }

        const { startDate, endDate } = row.dataset;

        if (!startDate || !endDate) return '';
        if (today < startDate) return 'upcoming';
        if (today > endDate) return 'completed';

        return 'active';
    }

    function applyFilter(card) {
        const filter = card.querySelector('[data-list-filter]');
        const emptyState = card.querySelector('[data-empty-state]');
        const rows = [...card.querySelectorAll('[data-item-row]')];
        const selected = filter?.value || 'all';
        const today = localDateKey();
        let visibleCount = 0;

        rows.forEach((row) => {
            const visible = selected === 'all'
                || getItemStatus(row, card.dataset.managementCard, today) === selected;

            row.hidden = !visible;

            if (visible) {
                visibleCount += 1;

                const number = row.querySelector('.item-number');
                if (number) number.textContent = String(visibleCount);
            }
        });

        if (emptyState) {
            emptyState.hidden = visibleCount > 0;
            emptyState.textContent = rows.length === 0
                ? emptyState.dataset.emptyText
                : emptyState.dataset.filteredEmptyText;
        }
    }

    function openDeleteModal(button) {
        const modal = document.getElementById('deleteModal');
        const row = button.closest('[data-item-row]');

        if (!modal || !row) return;

        deleteTarget = row;
        previousFocus = button;

        const values = {
            type: button.dataset.type,
            name: button.dataset.name
        };

        document.getElementById('modalTitle').textContent =
            formatMessage(modal.dataset.titleTemplate, values);

        document.getElementById('modalDesc').textContent =
            formatMessage(modal.dataset.descriptionTemplate, values);

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
        const card = deleteTarget?.closest('[data-management-card]');

        if (!deleteTarget || !card) {
            closeDeleteModal();
            return;
        }

        // 더미 화면에서만 삭제합니다. 서버 또는 dummy.js 파일은 변경하지 않습니다.
        deleteTarget.remove();
        previousFocus = card.querySelector('[data-list-filter]');
        applyFilter(card);
        closeDeleteModal();
    }

    function initXpCard() {
        const progressBar = document.getElementById('xp-progress');
        const track = progressBar?.closest('[role="progressbar"]');

        if (!progressBar || !track) return;

        const rawValue = Number(progressBar.dataset.progress);
        const percent = Number.isFinite(rawValue)
            ? Math.min(100, Math.max(0, rawValue))
            : 0;

        if (xpFrame !== null) {
            cancelAnimationFrame(xpFrame);
            xpFrame = null;
        }

        track.setAttribute('aria-valuenow', String(percent));

        progressBar.style.transition = 'none';
        progressBar.style.width = '0%';

        // 초기 0% 상태를 먼저 레이아웃에 반영합니다.
        void progressBar.offsetWidth;

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

        dashboard?.querySelectorAll('[data-management-card]').forEach((card) => {
            card.querySelector('[data-list-filter]')?.addEventListener(
                'change',
                () => applyFilter(card)
            );

            applyFilter(card);
        });

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
        initXpCard();
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', initHome, { once: true });
    } else {
        initHome();
    }
})();