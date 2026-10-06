<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/resources/css/routine.css">
<script src="${pageContext.request.contextPath}/resources/js/routine.js" defer></script>

<title>일상을 새롭게, 상새</title>

</head>
<body data-page="routine" data-context-path="${pageContext.request.contextPath}" class="viewport-height">

    <!-- 헤더 포함 -->
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="routine-container margin-auto ds-py-7 space-y-6">
  <!-- 탭 메뉴 컨트롤 -->
  <div class="layout-row ds-gap-2 border-bottom-strong border-color-border ds-pb-2">
    <button id="tabExplore" onclick="App.routine.switchTab('explore')" class="tab-active ds-px-4 ds-py-2_5 radius-md weight-bold type-base">✨ 루틴 탐색</button>
    <button id="tabManage" onclick="App.routine.switchTab('manage')" class="ds-px-4 ds-py-2_5 radius-md weight-bold type-base color-muted">📅 루틴 관리</button>
  </div>

  <!-- 탭 1: 루틴 탐색 -->
  <section id="tab-explore" class="space-y-6">
    <%@ include file="/WEB-INF/views/routine/tab_explorer.jsp" %>
  </section>

  <!-- 탭 2: 루틴 관리 -->
  <section id="tab-manage" class="is-hidden space-y-6">
    <%@ include file="/WEB-INF/views/routine/tab_management.jsp" %>
  </section>
</main>

<!-- 공통 토스트 알림 -->
<div id="toast" class="toast position-fixed ds-bottom-6 ds-right-6 surface-text color-white ds-px-5 ds-py-3 radius-md shadow-modal ds-z-100 type-sm"></div>

</body>
</html>
