<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>상새</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/style-jsp.css">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/onboarding.css">
<script src="${pageContext.request.contextPath}/assets/js/onboarding.js" defer></script>
</head>
<body data-page="onboarding" data-context-path="${pageContext.request.contextPath}">
<main id="onboarding-viewport"><section class="step-section active">
<div class="onboarding-container">
<div class="subtitle">일상을 새롭게</div><div class="logo">상새</div>
<div class="social-login-group">
<c:forEach var="provider" items="${enabledProviders}">
<a class="social-btn" href="${pageContext.request.contextPath}/oauth/<c:out value="${provider}"/>">
<c:choose><c:when test="${provider == 'google'}">Sign in with Google</c:when><c:when test="${provider == 'x'}">Sign in with X</c:when><c:when test="${provider == 'apple'}">Sign in with Apple</c:when><c:otherwise><c:out value="${provider}"/> 로그인</c:otherwise></c:choose>
</a>
</c:forEach>
</div>
<div class="sub-links">이미 계정이 있으신가요? <a href="${pageContext.request.contextPath}/auth/login">이메일 로그인</a></div>
<div class="sub-links"><a href="${pageContext.request.contextPath}/auth/signup">이메일 회원가입</a></div>
</div>
</section></main>
</body></html>
