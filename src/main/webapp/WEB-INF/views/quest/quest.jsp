<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/quest.css">
<script type="text/javascript" src="https://dapi.kakao.com/v2/maps/sdk.js?appkey=<%= com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() != null ? com.dteam0928.sangsae.common.util.ConfigUtil.getKakaoJsApiKey() : "" %>&libraries=services,clusterer"></script>
<script src="${pageContext.request.contextPath}/assets/js/mapUtil.js" defer></script>
<script src="${pageContext.request.contextPath}/assets/js/quest.js" defer></script>

    <title>상새 - 퀘스트</title>

    <!-- 공통 CSS 로드 -->

</head>
<body data-page="quest" data-context-path="${pageContext.request.contextPath}">

    <!-- 헤더 포함 -->
    <%@ include file="/WEB-INF/views/common/header.jsp" %>

    <main class="container">
        <!-- 메인 탭 전환 버튼 -->
        <div class="main-tabs">
            <button class="main-tab-btn title active" onclick="App.quest.switchTab('explorer', this)">퀘스트 탐색</button>
            <button class="main-tab-btn title" onclick="App.quest.switchTab('management', this)">
                퀘스트 관리 <span id="plannedCountBadge" class="desc"></span>
            </button>
        </div>

        <!-- [탭 1] 퀘스트 탐색 탭 -->
        <div id="tab-explorer" class="tab-content active">
            <%@ include file="/WEB-INF/views/quest/tab_explorer.jsp" %>
        </div>

        <!-- [탭 2] 여행 관리 탭 (올바른 경로 적용) -->
        <div id="tab-management" class="tab-content">
            <%@ include file="/WEB-INF/views/quest/tab_management.jsp" %>
        </div>
    </main>

    <!-- 모달 불러오기 -->
    <%@ include file="/WEB-INF/views/modal/modal_head.jsp" %>

    <!-- 컨텍스트 메뉴 및 토스트 -->
    <div class="context-menu" id="rowMenu"></div>
    <div class="toast desc" id="toast"></div>
    
</body>
</html>
