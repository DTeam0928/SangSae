<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ include file="/views/common/head.jsp" %>

<header class="header">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/resources/css/logo.png" alt="로고">상새
    </a>
    <nav class="nav">
        <%-- 컨트롤러 매핑 주소 또는 WEB-INF 이하 경로에 맞게 URL 수정 --%>
        <a href="${pageContext.request.contextPath}/views/home/home_main.jsp" id="nav-home">홈</a>
        <a href="${pageContext.request.contextPath}/views/quest/quest_main.jsp" id="nav-quest">퀘스트</a>
        <a href="${pageContext.request.contextPath}/views/routine/routine_main.jsp" id="nav-routine">루틴</a>
        <a href="${pageContext.request.contextPath}/views/onboarding/onboarding_main.jsp" id="nav-onboarding">온보딩 테스트</a>
    </nav>
</header>