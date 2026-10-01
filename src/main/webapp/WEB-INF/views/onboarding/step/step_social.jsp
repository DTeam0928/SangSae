<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <title>onboarding</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">
    <style>
    /* ===== Step 1 (소셜 로그인) 고유 스타일 ===== */
    .onboarding-container {
        max-width: 400px;
        margin: 0 auto;
        padding: 20px;
    }

    .onboarding-container .subtitle {
        color: var(--color-text);
        font-family: var(--font-logo);
        font-size: var(--fs-b);
        font-weight: var(--fw-body);
        text-align: center;
        margin-bottom: 20px;
    }

    .onboarding-container .logo {
        display: block;
        width: 100%;
        color: var(--color-dark);
        font-family: var(--font-logo);
        font-size: var(--fs-a);
        font-weight: 400;
        text-align: center;
        margin: 0 auto 30px auto;
        line-height: 1;
        letter-spacing: normal;
    }

    .social-login-group {
        display: flex;
        flex-direction: column;
        gap: 10px;
    }

    .social-btn {
        width: 100%;
        height: 50px;
        background: var(--color-white);
        border: 1px solid var(--color-border);
        border-radius: var(--radius-md);
        font-size: var(--fs-body);
        font-weight: 700;
        color: var(--color-text);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 10px;
        transition: background 0.2s, border-color 0.2s, transform 0.2s;
        box-shadow: var(--shadow-card);
        cursor: pointer;
    }

    .social-btn:hover {
        background: var(--color-bg);
        border-color: var(--color-dark);
        transform: translateY(-1px);
    }

    .social-icon {
        font-size: 18px;
    }

    .sub-links {
        margin-top: 24px;
        font-size: var(--fs-small);
        color: var(--color-muted);
        text-align: center;
    }

    .sub-links a {
        color: var(--color-dark);
        text-decoration: none;
        font-weight: 700;
        margin-left: 6px;
        cursor: pointer;
    }

    .sub-links a:hover {
        text-decoration: underline;
    }
    </style>
</head>


<div class="onboarding-container">
    <div class="subtitle">일상을 새롭게</div>
    <div class="logo">상새</div>

    <div class="social-login-group">
        <button type="button" class="social-btn" onclick="navigateToStep(3)">
            <span class="social-icon">🌐</span> Sign in with Google
        </button>
        <button type="button" class="social-btn" onclick="navigateToStep(3)">
            <span class="social-icon">✖️</span> Sign in with X
        </button>
        <button type="button" class="social-btn" onclick="navigateToStep(3)">
            <span class="social-icon">🍎</span> Sign in with Apple
        </button>
    </div>

    <div class="sub-links">
        이미 계정이 있으신가요?
        <a href="javascript:void(0)" onclick="navigateToStep(2)">이메일 로그인</a>
    </div>
</div>