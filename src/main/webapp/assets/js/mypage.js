(function () {
    // 공용 api 객체를 선언하거나 변경하지 않는 마이페이지 전용 통신.
    const script = document.currentScript;
    const scriptUrl = script && script.src ? new URL(script.src, window.location.href) : null;
    const scriptPath = scriptUrl && scriptUrl.origin === window.location.origin
        ? scriptUrl.pathname.match(/^(.*)\/assets\/js\/mypage\.js$/) : null;
    const pagePath = window.location.pathname.match(/^(.*)\/mypage(?:\.jsp)?\/?$/);
    const explicitPath = script && script.getAttribute('data-context-path');
    const contextPath = (explicitPath != null ? explicitPath
        : scriptPath && scriptPath[1] ? scriptPath[1]
            : pagePath ? pagePath[1] : scriptPath ? scriptPath[1] : '').replace(/\/$/, '');

    async function request(path, data) {
        const url = contextPath + path;
        const options = {
            method: data === undefined ? 'GET' : 'POST',
            credentials: 'same-origin',
            headers: { Accept: 'application/json', 'X-Requested-With': 'XMLHttpRequest' }
        };
        if (data !== undefined) {
            const body = new URLSearchParams();
            Object.entries(data).forEach(([key, value]) => {
                (Array.isArray(value) ? value : [value]).forEach(item => {
                    if (item != null) body.append(key, String(item));
                });
            });
            options.body = body;
        }
        const response = await fetch(url, options);
        if (!(response.headers.get('content-type') || '').toLowerCase().includes('json')) {
            throw new Error(response.redirected
                ? '로그인 페이지 등으로 이동되었습니다. 로그인 상태를 확인해주세요.'
                : `마이페이지 API 응답 오류 (HTTP ${response.status}): ${url}. 서버 매핑과 배포 상태를 확인해주세요.`);
        }
        const result = await response.json();
        if (!result || typeof result !== 'object' || Array.isArray(result)) {
            throw new Error('마이페이지 API의 JSON 응답 형식이 올바르지 않습니다.');
        }
        return response.ok ? result : Object.assign({}, result, {
            success: false, message: result.message || `HTTP ${response.status}`
        });
    }

    function nicknameField() {
        return document.querySelector('#nicknameInput')
            || document.querySelector('input.form-input[placeholder="변경할 닉네임 입력"]');
    }

    function locationBadge() {
        const existing = document.querySelector('#locationStatusBadge');
        if (existing) return existing;
        const button = document.querySelector('button[onclick*="App.user.requestLocation"]');
        const card = button && button.closest('.dash-card');
        return card && card.querySelector('.badge');
    }

    function showToast(msg, icon = '✨') {
        const t = document.getElementById('toast');
        if (!t) return;
        t.textContent = icon + ' ' + msg;
        t.classList.add('show');
        clearTimeout(window.toastTimer);
        window.toastTimer = setTimeout(() => t.classList.remove('show'), 2500);
    }

    window.App = window.App || {};

    window.App.user = Object.assign(window.App.user || {}, {
        // 0. 초기 프로필 로드 (GET /api/me/profile)
        initProfile: async function() {
            try {
                const response = await request('/api/me/profile');
                if (response && response.success && response.data) {
                    const profile = response.data;

                    // 닉네임 기본값 채우기
                    const nicknameInput = nicknameField();
                    if (nicknameInput && profile.nickname) {
                        nicknameInput.value = profile.nickname;
                    }

                    // 관심사 선택 상태 반영
                    if (profile.interests && profile.interests.length > 0) {
                        profile.interests.forEach(item => {
                            const chip = document.querySelector(`.interest-chip[data-id="${item.id}"]`);
                            if (chip) {
                                if (item.selected) chip.classList.add('selected');
                                else chip.classList.remove('selected');
                            }
                        });
                    }

                    // 위치 허용 상태 반영
                    if (profile.locationPermitted) {
                        const badge = locationBadge();
                        if (badge) {
                            badge.textContent = "허용됨";
                            badge.className = "badge green";
                        }
                    }
                } else {
                    console.warn("프로필 조회 실패:", response && response.message, response && response.httpStatus);
                }
            } catch (e) {
                console.warn("프로필 데이터 초기화 실패:", e);
            }
        },

        // 1. 닉네임 변경 (POST /api/me/nickname)
        updateNickname: async function() {
            const nicknameInput = nicknameField();
            const newNickname = nicknameInput ? nicknameInput.value.trim() : '';

            if (!newNickname) {
                showToast("변경할 닉네임을 입력해주세요.");
                return;
            }

            try {
                const response = await request('/api/me/nickname', { nickname: newNickname });

                if (response.success) {
                    showToast(response.message || "닉네임이 성공적으로 변경되었습니다.");
                } else {
                    showToast(response.message || "닉네임 변경 실패");
                }
            } catch (error) {
                console.error("닉네임 변경 오류:", error);
                showToast(error.message || "서버 통신 중 오류가 발생했습니다.");
            }
        },

        // 2. 관심사 저장 (POST /api/me/interests)
        saveInterests: async function() {
            const selectedChips = document.querySelectorAll('.interest-chip.selected');
            const interestIds = Array.from(selectedChips).map(chip => chip.dataset.id);

            try {
                const response = await request('/api/me/interests', { interestIds });

                if (response.success) {
                    showToast(response.message || "관심사가 저장되었습니다.");
                } else {
                    showToast(response.message || "관심사 저장 실패");
                }
            } catch (error) {
                console.error("관심사 저장 오류:", error);
                showToast(error.message || "서버 통신 중 오류가 발생했습니다.");
            }
        },

        // 3. 위치 권한 저장 (POST /api/me/location)
        requestLocation: function() {
            if (!navigator.geolocation) {
                showToast("이 브라우저에서는 위치 서비스를 지원하지 않습니다.");
                return;
            }

            navigator.geolocation.getCurrentPosition(
                async (position) => {
                    const lat = position.coords.latitude;
                    const lon = position.coords.longitude;
                    try {
                        const response = await request('/api/me/location', { latitude: lat, longitude: lon });

                        if (response.success) {
                            showToast(response.message || "위치 정보가 저장되었습니다.");
                            const badge = locationBadge();
                            if (badge) {
                                badge.textContent = "허용됨";
                                badge.className = "badge green";
                            }
                        } else {
                            showToast(response.message || "위치 정보 저장 실패");
                        }
                    } catch (error) {
                        console.error("위치 저장 오류:", error);
                        showToast(error.message || "서버 통신 중 오류가 발생했습니다.");
                    }
                },
                (error) => {
                    console.error("위치 권한 거부/오류:", error);
                    showToast("위치 권한이 거부되었거나 위치를 가져올 수 없습니다.");
                }
            );
        },

        // 4. 테마 적용
        applyTheme: function() {
            const selected = document.querySelector('input[name="appTheme"]:checked');
            if (!selected) return;

            const theme = selected.value;
            if (theme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'dark');
                localStorage.setItem('theme', 'dark');
            } else {
                document.documentElement.removeAttribute('data-theme');
                localStorage.setItem('theme', 'light');
            }

            showToast("테마 설정이 저장되었습니다.");
        },

        // 초기 테마 적용
        initTheme: function() {
            const savedTheme = localStorage.getItem('theme');
            if (savedTheme === 'dark') {
                document.documentElement.setAttribute('data-theme', 'dark');
                const darkRadio = document.querySelector('input[name="appTheme"][value="dark"]');
                if (darkRadio) darkRadio.checked = true;
            } else {
                document.documentElement.removeAttribute('data-theme');
                const lightRadio = document.querySelector('input[name="appTheme"][value="light"]');
                if (lightRadio) lightRadio.checked = true;
            }
        }
    });

// DOM 로드 완료 시
    function initMyPage() {
        // 칩 토글 이벤트 바인딩
        document.querySelectorAll('.interest-chip').forEach(chip => {
            if (chip.dataset.mypageBound === 'true') return;
            chip.dataset.mypageBound = 'true';
            chip.addEventListener('click', function() {
                this.classList.toggle('selected');
            });
        });

        // 1. 테마 상태 복원
        try {
            window.App.user.initTheme();
        } catch (error) {
            console.warn("테마 상태 복원 실패:", error);
        }

        // 2. 프로필 정보 서버 조회
        window.App.user.initProfile();
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", initMyPage, { once: true });
    } else {
        initMyPage();
    }
})();
