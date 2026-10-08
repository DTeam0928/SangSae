<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/views/common/head.jsp" %>
    <title>일상을 새롭게, 상새</title>
</head>
<body data-page="onboarding" data-context-path="${pageContext.request.contextPath}">

    <main id="onboarding-viewport">
        <!-- Step 1: 소셜 로그인 -->
        <section id="step-1" class="step-section active">
            <jsp:include page="/views/onboarding/step/step_social.jsp" />
        </section>

        <!-- Step 2: 이메일 로그인 / 회원가입 -->
        <section id="step-2" class="step-section">
            <jsp:include page="/views/onboarding/step/step_auth.jsp" />
        </section>

        <!-- Step 3: 프로필 & 관심사 -->
        <section id="step-3" class="step-section">
            <jsp:include page="/views/onboarding/step/step_profile.jsp" />
        </section>

        <!-- Step 4: 위치 권한 안내 -->
        <section id="step-4" class="step-section">
            <jsp:include page="/views/onboarding/step/step_location.jsp" />
        </section>
    </main>

</body>
</html>
