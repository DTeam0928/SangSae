(() => {
'use strict';

        function navigateToStep(stepNumber) {
            const sections = document.querySelectorAll('.step-section');
            sections.forEach(sec => sec.classList.remove('active'));

            const target = document.getElementById('step-' + stepNumber);
            if (target) {
                target.classList.add('active');
                window.scrollTo(0, 0);
            }
        }

        document.addEventListener('DOMContentLoaded', function() {
            navigateToStep(1);
        });
    

    let isLoginMode = true;
    let isEmailChecked = false;

    function hideAuthMessages() {
        const msgs = document.querySelectorAll('#auth-form .error-msg, #auth-form .success-msg');
        msgs.forEach(msg => msg.style.display = 'none');
    }

    function switchMode(mode) {
        isLoginMode = (mode === 'login');
        document.getElementById('tab-login').classList.toggle('active', isLoginMode);
        document.getElementById('tab-signup').classList.toggle('active', !isLoginMode);
        document.getElementById('submit-btn').innerText = isLoginMode ? '로그인' : '회원가입';

        document.getElementById('dup-btn').style.display = isLoginMode ? 'none' : 'block';
        document.getElementById('email-guide').style.display = isLoginMode ? 'none' : 'block';
        document.getElementById('password-guide').style.display = isLoginMode ? 'none' : 'block';
        document.getElementById('password-confirm-group').style.display = isLoginMode ? 'none' : 'block';

        document.getElementById('email').value = '';
        document.getElementById('password').value = '';
        document.getElementById('password-confirm').value = '';
        isEmailChecked = false;
        hideAuthMessages();
    }

    function validateEmail(email) {
        return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
    }

    function validatePassword(password) {
        return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*]{8,}$/.test(password);
    }

    document.getElementById('email').addEventListener('input', function() {
        if (!isLoginMode) {
            isEmailChecked = false;
            document.getElementById('email-success').style.display = 'none';
        }
    });

    function checkDuplicate() {
        const email = document.getElementById('email').value;
        const emailError = document.getElementById('email-error');
        const emailSuccess = document.getElementById('email-success');

        hideAuthMessages();

        if (!validateEmail(email)) {
            emailError.innerText = '올바른 이메일 형식을 입력해 주세요.';
            emailError.style.display = 'block';
            return;
        }

        if (email === 'test@test.com') {
            emailError.innerText = '이미 가입된 이메일입니다.';
            emailError.style.display = 'block';
            isEmailChecked = false;
        } else {
            emailSuccess.style.display = 'block';
            isEmailChecked = true;
        }
    }

    function handleSubmitAuth() {
        const email = document.getElementById('email').value;
        const password = document.getElementById('password').value;
        const passwordConfirm = document.getElementById('password-confirm').value;

        hideAuthMessages();

        if (isLoginMode) {
            if (!validateEmail(email)) {
                document.getElementById('email-error').innerText = '올바른 이메일 형식을 입력해 주세요.';
                document.getElementById('email-error').style.display = 'block';
                return;
            }
            if (!password) {
                alert('비밀번호를 입력해 주세요.');
                return;
            }
            location.href = App.url('/views/home/home_main.jsp');
        } else {
            if (!isEmailChecked) {
                alert('이메일 중복확인을 진행해 주세요.');
                return;
            }
            if (!validatePassword(password)) {
                document.getElementById('password-error').style.display = 'block';
                return;
            }
            if (password !== passwordConfirm) {
                document.getElementById('password-confirm-error').style.display = 'block';
                return;
            }
            navigateToStep(3);
        }
    }


    function requestLocationPermission() {
        if ("geolocation" in navigator) {
            navigator.geolocation.getCurrentPosition(
                function(position) {
                    alert('위치 권한이 허용되었습니다. 홈 화면으로 이동합니다.');
                    location.href = App.url('/views/home/home_main.jsp');
                },
                function(error) {
                    console.warn(`ERROR(${error.code}): ${error.message}`);
                    alert('위치 권한이 거부되었거나 가져올 수 없습니다. 수동 지역 선택 모드로 홈 화면으로 이동합니다.');
                    location.href = App.url('/views/home/home_main.jsp');
                },
                { timeout: 10000, maximumAge: 0 }
            );
        } else {
            alert('이 브라우저는 위치 정보를 지원하지 않습니다. 홈 화면으로 이동합니다.');
            location.href = App.url('/views/home/home_main.jsp');
        }
    }

    function skipLocationPermission() {
        alert('위치 권한 설정을 건너뛰었습니다. 지도 및 퀘스트 탐색 시 수동으로 지역을 선택할 수 있습니다.');
        location.href = App.url('/views/home/home_main.jsp');
    }


    function toggleChip(element) {
        const selectedChips = document.querySelectorAll('.interest-chip.selected');
        const chipError2 = document.getElementById('chip-error2');
        const chipError = document.getElementById('chip-error');
        chipError2.style.display = 'none';

        if (element.classList.contains('selected')) {
            element.classList.remove('selected');
            chipError.style.display = 'none';
        } else {
            if (selectedChips.length >= 5) {
                chipError2.style.display = 'block';
                return;
            }
            element.classList.add('selected');
            chipError.style.display = 'none';
        }
    }

    function hideNicknameError() {
        const nicknameInput = document.getElementById('nickname');
        const nicknameError = document.getElementById('nickname-error');
        if (nicknameInput.value.trim().length > 0) {
            nicknameError.style.display = 'none';
        }
    }

    function handleSubmitProfile() {
        const nicknameInput = document.getElementById('nickname');
        const nicknameError = document.getElementById('nickname-error');
        const chipError = document.getElementById('chip-error');
        const trimmedVal = nicknameInput.value.trim();

        if (trimmedVal.length === 0 || trimmedVal.length > 15) {
            nicknameError.style.display = 'block';
            nicknameInput.focus();
            return;
        } else {
            nicknameError.style.display = 'none';
        }

        chipError.style.display = 'none';
        App.storage.set('q_profile', JSON.stringify({nickname: trimmedVal, interests: [...document.querySelectorAll('.interest-chip.selected')].map(x => x.textContent)}));

        navigateToStep(4);
    }

App.onboarding = {navigateToStep, hideAuthMessages, switchMode, validateEmail, validatePassword, checkDuplicate, handleSubmitAuth, requestLocationPermission, skipLocationPermission, toggleChip, hideNicknameError, handleSubmitProfile};
})();
