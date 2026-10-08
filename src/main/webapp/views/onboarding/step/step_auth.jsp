<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<style>
    /* 폼 입력창 스타일 (디자인 토큰 활용) */
    .form-input {
        background-color: var(--surface-base);
        border: 1px solid var(--border-color);
        border-radius: var(--radius-md);
        color: var(--text-main);
        padding: 10px 14px;
        font-size: 14px;
        font-family: var(--font-family-base);
        outline: none;
        transition: border-color var(--transition-fast);
   }

    .form-input:focus {
        border-color: var(--border-interactive);
    }

    /* 탭 전환 스타일 */
    .tab {
        color: var(--text-muted);
        transition: background-color var(--transition-fast), color var(--transition-fast);
    }

    .tab.active {
        background-color: var(--surface-base);
        color: var(--primary-500);
        box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
    }

    /* 메시지 기본 숨김 처리 (JS나 조건문으로 제어) */
    .error-msg, .success-msg {
        display: none;
    }
</style>

<!-- card-nest와 유틸리티 클래스를 조합해 감싸는 컨테이너 구성 -->
<div class="auth-container card-nest mx-auto" style="max-width: 440px; margin-top: 60px;">

    <!-- 로고 영역: flex, items-center, text-primary 등 유틸 활용 -->
    <div class="logo flex items-center justify-center cursor-pointer mb-24 font-bold text-xl text-primary" onclick="App.onboarding.navigateToStep(1)">
        <img src="${pageContext.request.contextPath}/views/common/logo.png" alt="로고 이미지" style="width: 32px; height: 32px; margin-right: 8px;">상새
    </div>

    <!-- 탭 메뉴: flex와 rounded-md 유틸 활용 -->
    <div class="tab-group flex mb-24 rounded-md overflow-hidden" style="background-color: var(--surface-raised);">
        <div class="tab flex-1 text-center py-12 cursor-pointer font-medium active" onclick="App.onboarding.switchMode('login')" id="tab-login">이메일 로그인</div>
        <div class="tab flex-1 text-center py-12 cursor-pointer font-medium" onclick="App.onboarding.switchMode('signup')" id="tab-signup">회원가입</div>
    </div>

    <!-- 폼 영역: flex-col 활용 -->
    <form id="auth-form" action="${pageContext.request.contextPath}/auth/login" method="POST" class="flex flex-col">

        <!-- 전역 에러 메시지 -->
        <c:if test="${not empty errors.global}">
            <div class="error-msg global-error mb-16 text-xs text-error" style="display: block;">${errors.global}</div>
        </c:if>

        <!-- 이메일 입력 그룹 -->
        <div class="form-group mb-16 flex flex-col">
            <label class="form-label text-sm font-medium mb-8" for="email">이메일</label>
            <div class="input-wrap flex" style="gap: 8px;">
                <input type="email" id="email" name="email" class="form-input w-full" placeholder="example@questrip.com" value="${param.email}">
                <!-- 디자인 토큰 기반 .btn-secondary 재사용 -->
                <button type="button" id="dup-btn" class="onboarding-detail-1 dup-btn btn-secondary text-sm" style="padding: 10px 14px; white-space: nowrap;" onclick="App.onboarding.checkDuplicate()">중복확인</button>
            </div>
            <div class="guide-msg text-xs text-secondary mt-8" id="email-guide">수신 가능한 이메일 주소를 입력해 주세요.</div>

            <div class="error-msg text-xs text-error mt-8" id="email-error" style="${not empty errors.email ? 'display: block;' : ''}">
                ${not empty errors.email ? errors.email : '올바른 이메일 형식을 입력해 주세요.'}
            </div>
            <div class="success-msg text-xs text-primary mt-8" id="email-success">사용 가능한 이메일입니다.</div>
        </div>

        <!-- 비밀번호 입력 그룹 -->
        <div class="form-group mb-16 flex flex-col">
            <label class="form-label text-sm font-medium mb-8" for="password">비밀번호</label>
            <input type="password" id="password" name="password" class="form-input w-full" placeholder="비밀번호를 입력해 주세요">
            <div class="guide-msg text-xs text-secondary mt-8" id="password-guide">영문, 숫자 포함 8자 이상</div>

            <div class="error-msg text-xs text-error mt-8" id="password-error" style="${not empty errors.password ? 'display: block;' : ''}">
                ${not empty errors.password ? errors.password : '비밀번호 조건을 충족해 주세요.'}
            </div>
        </div>

        <!-- 비밀번호 확인 입력 그룹 -->
        <div class="onboarding-detail-1 form-group mb-24 flex flex-col" id="password-confirm-group">
            <label class="form-label text-sm font-medium mb-8" for="password-confirm">비밀번호 확인</label>
            <input type="password" id="password-confirm" name="passwordConfirm" class="form-input w-full" placeholder="비밀번호를 다시 입력해 주세요">

            <div class="error-msg text-xs text-error mt-8" id="password-confirm-error" style="${not empty errors.passwordConfirm ? 'display: block;' : ''}">
                ${not empty errors.passwordConfirm ? errors.passwordConfirm : '비밀번호가 일치하지 않습니다.'}
            </div>
        </div>

        <!-- 제출 버튼: 디자인 토큰 기반 .btn-primary 및 w-full 유틸 활용 -->
        <button type="submit" class="submit-btn btn-primary w-full" id="submit-btn">로그인</button>
    </form>
</div>