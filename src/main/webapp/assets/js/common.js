/* Shared browser services. Page modules export only App.<page> handlers. */
(() => {
    'use strict';

    function dateKey(date = new Date()) {
        return date.getFullYear() + '-' + String(date.getMonth()+1).padStart(2,'0') + '-' + String(date.getDate()).padStart(2,'0');
    }

    function escape(value) {
        return String(value ?? '').replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
    }

    window.App = Object.assign(window.App || {}, {
        dateKey,
        escape,
        url(path) { return (document.body?.dataset.contextPath || '') + path; }
    });

    document.addEventListener('DOMContentLoaded', () => {
        const page = document.body.dataset.page;
        document.querySelectorAll('.nav a').forEach(link => {
            const active = link.id === 'nav-' + page;
            link.classList.toggle('active', active);
            if (active) link.setAttribute('aria-current','page'); else link.removeAttribute('aria-current');
        });

        // Watch modal visibility without changing existing page handlers.
        let previousFocus = null;
        document.querySelectorAll('.modal-bg').forEach(layer => {
            const panel = layer.querySelector('.modal') || layer.firstElementChild;
            layer.setAttribute('aria-hidden','true');
            if (panel) { panel.setAttribute('role','dialog'); panel.setAttribute('aria-modal','true'); panel.tabIndex=-1; }
            new MutationObserver(() => {
                const open = layer.classList.contains('show');
                layer.setAttribute('aria-hidden', String(!open));
                if (open) {
                    previousFocus = document.activeElement;
                    (layer.querySelector('button,input,select,textarea,[tabindex]') || panel)?.focus();
                } else if (previousFocus?.isConnected) {
                    previousFocus.focus();
                }
            }).observe(layer, { attributes: true, attributeFilter: ['class'] });
        });

        document.addEventListener('keydown', event => {
            const layers = [...document.querySelectorAll('.modal-bg.show')];
            const layer = layers.at(-1);
            if (!layer) return;

            if (event.key === 'Escape') {
                event.preventDefault();
                layer.classList.remove('show');
            }

            if (event.key === 'Tab') {
                const controls = [...layer.querySelectorAll('button,input,select,textarea,a[href],[tabindex]')].filter(x => !x.disabled && x.tabIndex >= 0 && x.getClientRects().length);
                const first = controls[0], last = controls.at(-1);
                if (!first) { event.preventDefault(); return; }
                if (event.shiftKey && document.activeElement === first) {
                    event.preventDefault();
                    last.focus();
                } else if (!event.shiftKey && document.activeElement === last) {
                    event.preventDefault();
                    first.focus();
                }
            }
        });
    });
})();