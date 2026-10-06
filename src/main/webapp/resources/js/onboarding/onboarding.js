window.App = window.App || {};

App.onboarding = {
    /**
     * 1. 특정 온보딩 단계(Step)로 이동
     * @param {number} step - 이동할 스텝 번호
     */
    navigateToStep(step) {
        // 프로젝트 환경의 Context Path (JSP 메인 페이지 등에서 전역 설정)
        const contextPath = window.contextPath || '';

        switch (step) {
            case 1:
                window.location.href = `${contextPath}/onboarding/login`;
                break;
            case 2:
                window.location.href = `${contextPath}/onboarding/login`;
                break;
            case 3:
                window.location.href = `${contextPath}/onboarding/profile`;
                break;
            case 4:
                window.location.href = `${contextPath}/onboarding/location`;
                break;
            default:
        }
    },

    /**
     * 2. 이메일 로그인 / 회원가입 탭 전환
     * @param {string} mode - 'login' 또는 'signup'
     */
    switchMode(mode) {
        const authForm = document.getElementById('auth-form');
        const tabLogin = document.getElementById('tab-login');
        const tabSignup = document.getElementById('tab-signup');
        const submitBtn = document.getElementById('submit-btn');
        const contextPath = window.contextPath || '';

        // 회원가입 전용 요소 (비밀번호 확인, 중복확인 버튼 등)
        const signupOnlyElements = document.querySelectorAll('.onboarding-detail-1');

        // 메시지 영역 요소 초기화
        const emailError = document.getElementById('email-error');
        const emailSuccess = document.getElementById('email-success');
        const passwordConfirmError = document.getElementById('password-confirm-error');

        // 메시지 숨김 및 초기화
        if (emailError) emailError.style.display = 'none';
        if (emailSuccess) emailSuccess.style.display = 'none';
        if (passwordConfirmError) passwordConfirmError.style.display = 'none';

        if (mode === 'signup') {
            // 회원가입 모드 활성화
            tabLogin.classList.remove('active');
            tabSignup.classList.add('active');

            // 폼 Action URL 변경
            authForm.action = `${contextPath}/auth/signup`;
            submitBtn.textContent = '회원가입';

            // 회원가입 전용 요소 보이기
            signupOnlyElements.forEach(el => {
                el.style.display = ''; // 기본 display 상태(flex 등)로 복원
            });

        } else {
            // 로그인 모드 활성화 (기본)
            tabSignup.classList.remove('active');
            tabLogin.classList.add('active');

            // 폼 Action URL 변경
            authForm.action = `${contextPath}/auth/login`;
            submitBtn.textContent = '로그인';

            // 회원가입 전용 요소 숨기기
            signupOnlyElements.forEach(el => {
                el.style.display = 'none';
            });
        }
    },

    /**
     * 3. 이메일 중복 확인 AJAX 로직
     */
    checkDuplicate() {
        const emailInput = document.getElementById('email');
        const emailError = document.getElementById('email-error');
        const emailSuccess = document.getElementById('email-success');
        const contextPath = window.contextPath || '';

        if (!emailInput) return;

        const emailValue = emailInput.value.trim();

        // 1. 이메일 유효성 간단 검사 (Regex)
        const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
        if (!emailValue || !emailRegex.test(emailValue)) {
            if (emailSuccess) emailSuccess.style.display = 'none';
            if (emailError) {
                emailError.textContent = '올바른 이메일 형식을 입력해 주세요.';
                emailError.style.display = 'block';
            }
            emailInput.focus();
            return;
        }

        // 2. AJAX (Fetch API) 중복 확인 요청
        fetch(`${contextPath}/auth/check-email?email=${encodeURIComponent(emailValue)}`, {
            method: 'GET',
            headers: {
                'Accept': 'application/json'
            }
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error('서버 통신 오류');
                }
                return response.json();
            })
            .then(data => {
                // 서버 응답 규격 예시: { isDuplicate: true/false }
                if (data.isDuplicate) {
                    if (emailSuccess) emailSuccess.style.display = 'none';
                    if (emailError) {
                        emailError.textContent = '이미 사용 중인 이메일입니다.';
                        emailError.style.display = 'block';
                    }
                } else {
                    if (emailError) emailError.style.display = 'none';
                    if (emailSuccess) {
                        emailSuccess.textContent = '사용 가능한 이메일입니다.';
                        emailSuccess.style.display = 'block';
                    }
                }
            })
            .catch(error => {
                console.error('이메일 중복 확인 중 오류:', error);
                if (emailSuccess) emailSuccess.style.display = 'none';
                if (emailError) {
                    emailError.textContent = '중복 확인 중 오류가 발생했습니다. 다시 시도해 주세요.';
                    emailError.style.display = 'block';
                }
            });
    }
};

// 페이지 로드 시 기본 초기화 (기본 로그인 모드 세팅)
document.addEventListener('DOMContentLoaded', () => {
    // URL 파라미터나 기본값에 따라 로그인 탭 상태로 초기 배치
    App.onboarding.switchMode('login');
});