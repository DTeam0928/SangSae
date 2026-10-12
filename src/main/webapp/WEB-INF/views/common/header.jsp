<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<script src="${pageContext.request.contextPath}/assets/js/api-client.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/auth.js" defer></script>
<header class="header">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/assets/css/logo.png" alt="로고">상새
    </a>
    <nav class="nav" aria-label="주 메뉴">
        <a href="${pageContext.request.contextPath}/home" id="nav-home">홈</a>
        <a href="${pageContext.request.contextPath}/quest" id="nav-quest">퀘스트</a>
        <a href="${pageContext.request.contextPath}/routine" id="nav-routine">루틴</a>
        <button class="header-logout" onclick="handleLogout()">로그아웃</button>
    </nav>
</header>
