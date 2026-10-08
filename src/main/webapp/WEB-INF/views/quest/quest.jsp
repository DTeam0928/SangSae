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

    <!-- ==========================================
         인라인 모달 레이어 정의
    =========================================== -->
    <!-- 1. 지역 선택 모달 -->
    <div class="modal-bg" id="regionModal" onclick="App.quest.closeModalOutside(event, 'regionModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">탐색할 지역 선택</h2>
          <button class="close" onclick="App.quest.closeModal('regionModal')">×</button>
        </div>
        <div class="quest-detail-1">
          <p class="desc">부산 지역 필터가 기본 적용되어 있습니다.</p>
          <button class="btn btn-primary" onclick="App.quest.applyRegions()">부산 전체 적용</button>
        </div>
      </div>
    </div>

    <!-- 2. 내 컬렉션 모달 -->
    <div class="modal-bg" id="collectionModal" onclick="App.quest.closeModalOutside(event, 'collectionModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">♡ 내 컬렉션</h2>
          <button class="close" onclick="App.quest.closeModal('collectionModal')">×</button>
        </div>
        <div id="collectionBody"></div>
      </div>
    </div>

    <!-- 3. 여행 선택/전환 모달 -->
    <div class="modal-bg" id="tripModal" onclick="App.quest.closeModalOutside(event, 'tripModal')">
      <div class="quest-detail-2 modal">
        <div class="modal-head">
          <h2 class="title">내 여행 목록</h2>
          <button class="close" onclick="App.quest.closeModal('tripModal')">×</button>
        </div>
        <div id="tripListContent"></div>
      </div>
    </div>

    <!-- 4. 퀘스트 상세 모달 -->
    <div class="modal-bg" id="detailModal" onclick="App.quest.closeModalOutside(event, 'detailModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">퀘스트 상세</h2>
          <button class="close" onclick="App.quest.closeModal('detailModal')">×</button>
        </div>
        <div id="detailContent"></div>
      </div>
    </div>

    <!-- 5. 퀘스트 생성 모달 -->
    <div class="modal-bg" id="createModal" onclick="App.quest.closeModalOutside(event, 'createModal')">
      <div class="modal">
        <div class="modal-head">
          <h2 class="title">✨ 나만의 퀘스트 만들기</h2>
          <button class="close" onclick="App.quest.closeModal('createModal')">×</button>
        </div>
        <div class="quest-detail-3">
          <label class="form-label">퀘스트 제목</label>
          <input id="newTitle" class="form-input" placeholder="예: 광안리 일몰 보기">
        </div>
        <button class="quest-detail-4 btn btn-primary" onclick="App.quest.createQuest()">퀘스트 발행하기</button>
      </div>
    </div>

    <!-- 컨텍스트 메뉴 및 토스트 -->
    <div class="context-menu" id="rowMenu"></div>
    <div class="toast desc" id="toast"></div>
    
</body>
</html>
