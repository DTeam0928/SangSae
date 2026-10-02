<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="onboarding-container">
    <div class="subtitle">일상을 새롭게</div>
    <div class="logo">상새</div>

    <div class="social-login-group">
        <button type="button" class="social-btn" onclick="App.onboarding.navigateToStep(3)">
            <span class="social-icon">🌐</span> Sign in with Google
        </button>
        <button type="button" class="social-btn" onclick="App.onboarding.navigateToStep(3)">
            <span class="social-icon">✖️</span> Sign in with X
        </button>
        <button type="button" class="social-btn" onclick="App.onboarding.navigateToStep(3)">
            <span class="social-icon">🍎</span> Sign in with Apple
        </button>
    </div>

    <div class="sub-links">
        이미 계정이 있으신가요?
        <a href="javascript:void(0)" onclick="App.onboarding.navigateToStep(2)">이메일 로그인</a>
    </div>
</div>
