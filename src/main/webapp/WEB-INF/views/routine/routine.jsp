<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/routine.css">
<script type="text/javascript" src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=<%= com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() != null ? com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() : "" %>&libraries=services,clusterer"></script>
<script src="${pageContext.request.contextPath}/assets/js/mapUtil.js?v=<%= System.currentTimeMillis() %>" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/routine.js?v=<%= System.currentTimeMillis() %>" defer></script>

<title>일상을 새롭게, 상새</title>

</head>
<body data-page="routine" data-context-path="${pageContext.request.contextPath}" class="viewport-height">

    <!-- 헤더 포함 -->
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

<main class="routine-container margin-auto ds-py-7 space-y-6">
  <!-- 탭 메뉴 컨트롤 -->
  <div class="main-tabs">
    <button id="tabExplore" onclick="App.routine.switchTab('explorer', this)" class="main-tab-btn title active">✨ 루틴 탐색</button>
    <button id="tabManage" onclick="App.routine.switchTab('management', this)" class="main-tab-btn title">📅 루틴 관리</button>
  </div>

  <!-- 탭 1: 루틴 탐색 -->
  <section id="tab-explore" class="tab-content active">
    <%@ include file="/WEB-INF/views/routine/tab_explorer.jsp" %>
  </section>

  <!-- 탭 2: 루틴 관리 -->
  <section id="tab-manage" class="tab-content">
    <%@ include file="/WEB-INF/views/routine/tab_management.jsp" %>
  </section>
</main>

    <!-- 모달 불러오기 -->
    <%@ include file="/WEB-INF/views/modal/modal_head.jsp" %>

    <!-- 공통 토스트 알림 -->
<div id="toast" class="toast position-fixed ds-bottom-6 ds-right-6 surface-text color-white ds-px-5 ds-py-3 radius-md shadow-modal ds-z-100 type-sm"></div>

</body>
</html>
