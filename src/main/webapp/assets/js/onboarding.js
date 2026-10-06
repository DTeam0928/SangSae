(() => {
    'use strict';

    // 컨텍스트 패스 가져오기 (필요 시 global 변수 활용 가능)
    const contextPath = document.body.dataset.contextPath || '';

    let isLoginMode = document.body.dataset.authMode !== 'signup';
    let checkedEmail = document.body.dataset.emailChecked === 'true'
        ? document.getElementById('email')?.value.trim().toLowerCase() : null;
    let checkVersion = 0;
    function hideAuthMessages() {
        document.querySelectorAll('#auth-form .error-msg, #auth-form .success-msg')
            .forEach(element => element.style.display = 'none');
    }
    function applyMode() {
        const form = document.getElementById('auth-form');
        if (!form) return;
        form.action = contextPath + (isLoginMode ? '/auth/login' : '/auth/signup');
        document.getElementById('tab-login').classList.toggle('active', isLoginMode);
        document.getElementById('tab-signup').classList.toggle('active', !isLoginMode);
        document.getElementById('submit-btn').textContent = isLoginMode ? '로그인' : '회원가입';
        ['dup-btn', 'email-guide', 'password-guide', 'password-confirm-group'].forEach(id => {
            document.getElementById(id).style.display = isLoginMode ? 'none' : 'block';
        });
        document.getElementById('password-confirm').required = !isLoginMode;
        document.getElementById('email-success').style.display = !isLoginMode && checkedEmail ? 'block' : 'none';
    }
    function switchMode(mode) {
        isLoginMode = mode === 'login';
        checkVersion++;
        checkedEmail = null;
        hideAuthMessages();
        ['password', 'password-confirm'].forEach(id => document.getElementById(id).value = '');
        applyMode();
    }
    function validateEmail(email) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email); }
    function validatePassword(password) { return password.length > 0; } // Java validates password policy.
    async function checkDuplicate() {
        const input = document.getElementById('email');
        const email = input.value.trim();
        const error = document.getElementById('email-error');
        hideAuthMessages();
        checkedEmail = null;
        const version = ++checkVersion;
        if (!validateEmail(email)) {
            error.textContent = '올바른 이메일 형식을 입력해 주세요.';
            error.style.display = 'block'; return;
        }
        const res = await api.postForm(contextPath + '/api/auth/email-check', { email });
        if (version !== checkVersion || isLoginMode || input.value.trim() !== email) return;
        if (res.success === true && typeof res.data?.email === 'string') {
            checkedEmail = res.data.email.trim().toLowerCase();
            document.getElementById('email-success').textContent = res.message || '사용 가능한 이메일입니다.';
            document.getElementById('email-success').style.display = 'block';
        } else {
            error.textContent = res.message || '중복 확인에 실패했습니다.';
            error.style.display = 'block';
        }
    }
    function handleSubmitAuth(event) {
        const input = document.getElementById('email');
        input.value = input.value.trim();
        const password = document.getElementById('password').value;
        const confirm = document.getElementById('password-confirm').value;
        let message = '';
        if (!validateEmail(input.value)) message = '올바른 이메일을 입력해 주세요.';
        else if (!password) message = '비밀번호를 입력해 주세요.';
        else if (!isLoginMode && checkedEmail !== input.value.toLowerCase()) message = '이메일 중복 확인을 진행해 주세요.';
        else if (!isLoginMode && password !== confirm) message = '비밀번호가 일치하지 않습니다.';
        if (message) { event.preventDefault(); alert(message); return false; }
        return true;
    }
    const emailInput = document.getElementById('email');
    if (emailInput) emailInput.addEventListener('input', () => {
        checkVersion++; checkedEmail = null;
        document.getElementById('email-success').style.display = 'none';
    });

    // 7. [O-03] 위치 권한 요청 및 전송 (POST /onboarding/location)
    function submitLocation(action, latitude = null, longitude = null) {
        const form = document.createElement('form');
        form.method = 'POST';
        form.action = `${contextPath}/onboarding/location`;

        const actionInput = document.createElement('input');
        actionInput.type = 'hidden';
        actionInput.name = 'action';
        actionInput.value = action;
        form.appendChild(actionInput);

        if (latitude !== null && longitude !== null) {
            const latInput = document.createElement('input');
            latInput.type = 'hidden';
            latInput.name = 'latitude';
            latInput.value = latitude;
            form.appendChild(latInput);

            const lngInput = document.createElement('input');
            lngInput.type = 'hidden';
            lngInput.name = 'longitude';
            lngInput.value = longitude;
            form.appendChild(lngInput);
        }

        document.body.appendChild(form);
        form.submit();
    }

    function requestLocationPermission() {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    submitLocation('allow', position.coords.latitude, position.coords.longitude);
                },
                function(error) {
                    console.warn(`Geolocation Error (${error.code}): ${error.message}`);
                    submitLocation('deny');
                },
                { timeout: 10000, maximumAge: 0 }
            );
        } else {
            submitLocation('deny');
        }
    }

    function skipLocationPermission() {
        submitLocation('later');
    }

    // 8. [O-02] 프로필 및 관심사 선택
    function maxInterestCount() {
        const value = Number(document.getElementById('profile-form')?.dataset.maxInterestCount);
        return Number.isInteger(value) && value >= 0 ? value : 5;
    }
    function toggleChip(input) {
        const count = document.querySelectorAll('input[name="interestIds"]:checked').length;
        const error = document.getElementById('chip-error2');
        if (count > maxInterestCount()) {
            input.checked = false;
            error.style.display = 'block';
        } else {
            error.style.display = 'none';
        }
        input.closest('.interest-chip').classList.toggle('selected', input.checked);
    }

    function hideNicknameError() {
        const nicknameInput = document.getElementById('nickname');
        const nicknameError = document.getElementById('nickname-error');
        if (nicknameInput && nicknameInput.value.trim().length > 0) {
            if (nicknameError) nicknameError.style.display = 'none';
        }
    }

    function handleSubmitProfile(event) {
        const input = document.getElementById('nickname');
        input.value = input.value.trim();
        const valid = input.value.length > 0 && input.value.length <= 15;
        document.getElementById('nickname-error').style.display = valid ? 'none' : 'block';
        if (!valid) { event.preventDefault(); input.focus(); return false; }
        const count = document.querySelectorAll('input[name="interestIds"]:checked').length;
        if (count > maxInterestCount()) {
            document.getElementById('chip-error2').style.display = 'block';
            event.preventDefault(); return false;
        }
        return true;
    }

    document.addEventListener('DOMContentLoaded', applyMode);

    // 전역 네임스페이스 매핑
    window.App = window.App || {};
    window.App.onboarding = {
        hideAuthMessages,
        switchMode,
        validateEmail,
        validatePassword,
        checkDuplicate,
        handleSubmitAuth,
        requestLocationPermission,
        skipLocationPermission,
        toggleChip,
        hideNicknameError,
        handleSubmitProfile
    };
})();