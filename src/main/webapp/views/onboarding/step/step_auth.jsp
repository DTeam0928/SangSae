<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<div class="auth-container">
    <div class="logo" onclick="App.onboarding.navigateToStep(1)">
        <img src="${pageContext.request.contextPath}/views/common/logo.png" alt="로고 이미지">상새
    </div>

    <!-- 탭 메뉴 -->
    <div class="tab-group">
        <div class="tab active" onclick="App.onboarding.switchMode('login')" id="tab-login">이메일 로그인</div>
        <div class="tab" onclick="App.onboarding.switchMode('signup')" id="tab-signup">회원가입</div>
    </div>

    <!-- 입력 폼 -->
    <div id="auth-form">
        <div class="form-group">
            <label class="form-label" for="email">이메일</label>
            <div class="input-wrap">
                <input type="email" id="email" class="form-input" placeholder="example@questrip.com">
                <button type="button" id="dup-btn" class="onboarding-detail-1 dup-btn" onclick="App.onboarding.checkDuplicate()">중복확인</button>
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

        <div class="onboarding-detail-1 form-group" id="password-confirm-group">
            <label class="form-label" for="password-confirm">비밀번호 확인</label>
            <input type="password" id="password-confirm" class="form-input" placeholder="비밀번호를 다시 입력해 주세요">
            <div class="error-msg" id="password-confirm-error">비밀번호가 일치하지 않습니다.</div>
        </div>

        <button type="button" class="submit-btn" id="submit-btn" onclick="App.onboarding.handleSubmitAuth()">로그인</button>
    </div>
</div>