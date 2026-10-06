<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>

<header class="header">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/assets/css/logo.png" alt="로고">상새
    </a>
    <nav class="nav" aria-label="주 메뉴">
        <a href="${pageContext.request.contextPath}/webapp/WEB-INF/views/home/home.jsp" id="nav-home">홈</a>
        <a href="${pageContext.request.contextPath}/WEB-INF/views/quest/quest_main.jsp" id="nav-quest">퀘스트</a>
        <a href="${pageContext.request.contextPath}/WEB-INF/views/routine/routine_main.jsp" id="nav-routine">루틴</a>
        <a href="${pageContext.request.contextPath}/WEB-INF/views/auth/landing.jsp" id="nav-onboarding">온보딩 테스트</a>
    </nav>
</header>
