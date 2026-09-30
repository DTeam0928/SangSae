<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>header</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">
</head>
<header class="header">
    <a href="${pageContext.request.contextPath}/" class="logo">
        <img src="${pageContext.request.contextPath}/views/common/logo.png" alt="상새 로고">상새
    </a>
    <nav class="nav">
        <a href="${pageContext.request.contextPath}/views/home/home_main.jsp" class="active">홈</a>
        <a href="${pageContext.request.contextPath}/views/quest/quest_main.jsp">퀘스트</a>
        <a href="${pageContext.request.contextPath}/views/routine/routine_main.jsp">루틴</a>
    </nav>
</header>