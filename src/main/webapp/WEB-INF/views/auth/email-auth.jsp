<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>상새</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/variables.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/onboarding.css">
<script src="${pageContext.request.contextPath}/assets/js/api-client.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/onboarding.js" defer></script>
</head>
<body data-auth-mode="${mode}" data-email-checked="${emailChecked}" data-page="onboarding" data-context-path="${pageContext.request.contextPath}">
<main id="onboarding-viewport"><section class="step-section active">


<div class="auth-container">
    <a class="logo" href="${pageContext.request.contextPath}/">
        <img src="${pageContext.request.contextPath}/assets/css/logo.png" alt="로고 이미지">상새
    </a>

    <!-- 탭 메뉴 -->
    <div class="tab-group">
        <div class="tab active" onclick="App.onboarding.switchMode('login')" id="tab-login">이메일 로그인</div>
        <div class="tab" onclick="App.onboarding.switchMode('signup')" id="tab-signup">회원가입</div>
    </div>

    <!-- [수정] form 태그로 변경 (기본 POST /auth/login) -->
    <form id="auth-form" action="${pageContext.request.contextPath}${mode == 'signup' ? '/auth/signup' : '/auth/login'}" method="POST" onsubmit="return App.onboarding.handleSubmitAuth(event)">

        <!-- 전역 에러 표시 -->
        <c:if test="${not empty errors.global}">
            <div class="error-msg global-error" style="display: block; margin-bottom: 15px;"><c:out value="${errors.global}"/></div>
        </c:if>

        <div class="form-group">
            <label class="form-label" for="email">이메일</label>
            <div class="input-wrap">
                <!-- [수정] name="email" 추가 및 이전 입력값 유지 -->
                <input type="email" id="email" name="email" class="form-input" placeholder="example@questrip.com" value="<c:out value="${not empty email ? email : param.email}"/>">
                <button type="button" id="dup-btn" class="onboarding-detail-1 dup-btn" onclick="App.onboarding.checkDuplicate()">중복확인</button>
            </div>
            <div class="guide-msg" id="email-guide">수신 가능한 이메일 주소를 입력해 주세요.</div>

            <!-- [수정] 서버에서 넘어온 email 인라인 에러 출력 -->
            <div class="error-msg" id="email-error" style="${not empty errors.email ? 'display: block;' : ''}">
                <c:out value="${not empty errors.email ? errors.email : '올바른 이메일 형식을 입력해 주세요.'}"/>
            </div>
            <div class="success-msg" id="email-success">사용 가능한 이메일입니다.</div>
        </div>

        <div class="form-group">
            <label class="form-label" for="password">비밀번호</label>
            <!-- [수정] name="password" 추가 -->
            <input type="password" id="password" name="password" class="form-input" placeholder="비밀번호를 입력해 주세요">
            <div class="guide-msg" id="password-guide">영문, 숫자 포함 8자 이상</div>

            <!-- [수정] 서버에서 넘어온 password 인라인 에러 출력 -->
            <div class="error-msg" id="password-error" style="${not empty errors.password ? 'display: block;' : ''}">
                <c:out value="${not empty errors.password ? errors.password : '비밀번호 조건을 충족해 주세요.'}"/>
            </div>
        </div>

        <div class="onboarding-detail-1 form-group" id="password-confirm-group">
            <label class="form-label" for="password-confirm">비밀번호 확인</label>
            <!-- [수정] name="passwordConfirm" 추가 -->
            <input type="password" id="password-confirm" name="passwordConfirm" class="form-input" placeholder="비밀번호를 다시 입력해 주세요">

            <!-- [수정] 서버에서 넘어온 passwordConfirm 인라인 에러 출력 -->
            <div class="error-msg" id="password-confirm-error" style="${not empty errors.passwordConfirm ? 'display: block;' : ''}">
                <c:out value="${not empty errors.passwordConfirm ? errors.passwordConfirm : '비밀번호가 일치하지 않습니다.'}"/>
            </div>
        </div>

        <!-- [수정] type="submit"으로 변경하여 폼 전송 활성화 -->
        <button type="submit" class="submit-btn" id="submit-btn">로그인</button>
    </form>
</div>
</section></main>
</body></html>
