<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<%@ include file="/WEB-INF/views/common/head.jsp" %>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/quest.css">
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
                여행 관리 <span id="plannedCountBadge" class="desc"></span>
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

    <!-- quest.jsp 또는 관련 메인 뷰 파일 하단에 삽입 -->
    <jsp:include page="/WEB-INF/views/modal/region_modal.jsp" />
    <jsp:include page="/WEB-INF/views/modal/bookmark_modal.jsp" />
    <jsp:include page="/WEB-INF/views/modal/trip_modal.jsp" />
    <jsp:include page="/WEB-INF/views/modal/quest_detail_modal.jsp" />
    <jsp:include page="/WEB-INF/views/modal/quest_create_modal.jsp" />

    <!-- 컨텍스트 메뉴 및 토스트 -->
    <div class="context-menu" id="rowMenu"></div>
    <div class="toast desc" id="toast"></div>
    
</body>
</html>
