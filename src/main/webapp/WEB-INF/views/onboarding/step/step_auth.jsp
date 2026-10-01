<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <title>onboarding</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">
    <style>
    /* ===== Step 2 (이메일 로그인/가입) 고유 스타일 ===== */
    .auth-container {
        width: min(400px, calc(100% - 40px));
        margin: 0 auto;
        background: var(--color-white);
        padding: 40px 30px;
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-card);
        border: 1px solid var(--color-border);
    }

    .auth-container .logo {
        display: flex;
        width: 100%;
        justify-content: center;
        align-items: center;
        text-align: center;
        font-family: var(--font-logo);
        font-size: 44px;
        font-weight: 400;
        color: var(--color-dark);
        margin: 0 auto 30px auto;
        cursor: pointer;
        line-height: 1;
    }

    .auth-container .logo img {
        height: 50px;
        width: auto;
        margin-right: 10px;
        object-fit: contain;
    }

    .tab-group {
        display: flex;
        margin-bottom: 24px;
        border-bottom: 1px solid var(--color-border);
    }

    .tab {
        flex: 1;
        text-align: center;
        padding: 12px;
        cursor: pointer;
        font-size: var(--fs-body);
        font-weight: 700;
        color: var(--color-muted);
        transition: color 0.2s, border-bottom 0.2s;
    }

    .tab.active {
        color: var(--color-dark);
        border-bottom: 2px solid var(--color-dark);
    }

    .form-group {
        margin-bottom: 16px;
    }

    .input-wrap {
        display: flex;
        gap: 8px;
    }

    .input-wrap .form-input {
        flex: 1;
    }

    .dup-btn {
        padding: 0 16px;
        background: var(--color-sub-bg);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-sm);
        font-size: var(--fs-small);
        font-weight: 700;
        color: var(--color-text);
        white-space: nowrap;
        transition: background 0.2s;
        cursor: pointer;
    }

    .dup-btn:hover {
        background: var(--color-border);
    }

    .guide-msg {
        color: var(--color-muted);
        font-size: var(--fs-small);
        margin-top: 6px;
        display: none;
    }

    .error-msg {
        color: var(--color-danger);
        font-size: var(--fs-small);
        margin-top: 6px;
        display: none;
    }

    .success-msg {
        color: var(--color-primary);
        font-size: var(--fs-small);
        margin-top: 6px;
        display: none;
    }

    .submit-btn {
        width: 100%;
        height: 50px;
        background: var(--color-dark);
        border: none;
        border-radius: var(--radius-md);
        color: var(--color-white);
        font-size: 15px;
        font-weight: 800;
        margin-top: 10px;
        transition: background 0.2s, transform 0.2s;
        cursor: pointer;
    }

    .submit-btn:hover {
        background: #0087dc;
        transform: translateY(-1px);
    }
    </style>
</head>

<div class="auth-container">
    <div class="logo" onclick="navigateToStep(1)">
        <img src="${pageContext.request.contextPath}/views/common/logo.png" alt="로고 이미지">상새
    </div>

    <!-- 탭 메뉴 -->
    <div class="tab-group">
        <div class="tab active" onclick="switchMode('login')" id="tab-login">이메일 로그인</div>
        <div class="tab" onclick="switchMode('signup')" id="tab-signup">회원가입</div>
    </div>

    <!-- 입력 폼 -->
    <div id="auth-form">
        <div class="form-group">
            <label class="form-label" for="email">이메일</label>
            <div class="input-wrap">
                <input type="email" id="email" class="form-input" placeholder="example@questrip.com">
                <button type="button" id="dup-btn" class="dup-btn" onclick="checkDuplicate()" style="display: none;">중복확인</button>
            </div>
            <div class="guide-msg" id="email-guide">수신 가능한 이메일 주소를 입력해 주세요.</div>
            <div class="error-msg" id="email-error">올바른 이메일 형식을 입력해 주세요.</div>
            <div class="success-msg" id="email-success">사용 가능한 이메일입니다.</div>
        </div>

        <div class="form-group">
            <label class="form-label" for="password">비밀번호</label>
            <input type="password" id="password" class="form-input" placeholder="비밀번호를 입력해 주세요">
            <div class="guide-msg" id="password-guide">영문, 숫자 포함 8자 이상</div>
            <div class="error-msg" id="password-error">비밀번호 조건을 충족해 주세요.</div>
        </div>

        <div class="form-group" id="password-confirm-group" style="display: none;">
            <label class="form-label" for="password-confirm">비밀번호 확인</label>
            <input type="password" id="password-confirm" class="form-input" placeholder="비밀번호를 다시 입력해 주세요">
            <div class="error-msg" id="password-confirm-error">비밀번호가 일치하지 않습니다.</div>
        </div>

        <button type="button" class="submit-btn" id="submit-btn" onclick="handleSubmitAuth()">로그인</button>
    </div>
</div>

<script>
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
            location.href = '${pageContext.request.contextPath}/views/home/home_main.jsp';
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
</script>