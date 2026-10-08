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
              <label class="form-label" for="newDescription">퀘스트 설명</label>
      <textarea id="newDescription" class="form-input" rows="3"
                placeholder="퀘스트 내용을 설명해 주세요"></textarea>

      <label class="form-label" for="newCategory">카테고리</label>
      <select id="newCategory" class="form-input">
        <option value="WALK">산책</option>
        <option value="CAFE">카페</option>
        <option value="EXERCISE">운동</option>
        <option value="CULTURE">문화</option>
        <option value="OTHER">기타</option>
      </select>

      <label class="form-label" for="newEstimatedMinutes">예상 소요 시간 (분)</label>
      <input id="newEstimatedMinutes" class="form-input"
             type="number" min="1" value="30">

      <label class="form-label" for="newRewardXp">보상 XP</label>
      <input id="newRewardXp" class="form-input"
             type="number" min="0" value="100">

      <label class="form-label" for="newAuthType">인증 방식</label>
      <select id="newAuthType" class="form-input"
              onchange="App.quest.toggleCreatePlaceSearch()">
        <option value="NONE">인증 없음</option>
        <option value="LOCATION">위치 인증 (GPS)</option>
        <option value="PHOTO">사진 인증</option>
        <option value="TEXT">텍스트 인증</option>
      </select>

      <!-- LOCATION 선택 시에만 표시 -->
      <div id="createPlaceSection" style="display:none;">
        <label class="form-label" for="createPlaceQuery">장소 검색</label>
        <div style="display:flex; gap:8px;">
          <input id="createPlaceQuery" class="form-input"
                 placeholder="예: 부산시민공원">
          <button type="button" class="btn btn-primary"
                  onclick="App.quest.searchCreatePlaces()">검색</button>
        </div>
        <div id="createPlaceResults"></div>
        <div id="createSelectedPlace"></div>
      </div>
      <label class="form-label" for="newVisibility">공개 범위</label>
      <select id="newVisibility" class="form-input">
        <option value="PUBLIC">전체 공개</option>
        <option value="PRIVATE">비공개</option>
      </select>
        </div>
        <button class="quest-detail-4 btn btn-primary" onclick="App.quest.createQuest()">퀘스트 발행하기</button>
      </div>
    </div>

	<!-- 위치 인증 반경 선택 모달 -->
	<div class="modal-bg" id="authRadiusModal"
	     onclick="App.quest.closeModalOutside(event, 'authRadiusModal')">

	    <div class="modal">
	        <div class="modal-head">
	            <h2 class="title">📍 위치 인증 반경 설정</h2>
	            <button type="button" class="close"
	                    onclick="App.quest.closeModal('authRadiusModal')">×</button>
	        </div>

	        <div style="margin-top: 20px;">
	            <div style="display:flex; justify-content:space-between;
	                        align-items:center; margin-bottom:12px;">

	                <label for="authRadiusM" class="subtitle">
	                    GPS 인증 허용 반경
	                </label>

	                <strong id="authRadiusValue">300m</strong>
	            </div>

	            <input type="range"
	                   id="authRadiusM"
	                   min="100"
	                   max="1000"
	                   step="50"
	                   value="300"
	                   style="width:100%; cursor:pointer;"
	                   oninput="
	                       document.getElementById('authRadiusValue').textContent =
	                           this.value + 'm';
	                       document.getElementById('authRadiusDescription').textContent =
	                           '목표 장소에서 ' + this.value +
	                           'm 이내에 도착하면 GPS 인증이 가능합니다.';
	                   ">

	            <div style="display:flex; justify-content:space-between;
	                        font-size:12px; margin-top:6px;">
	                <span>100m</span>
	                <span>1000m</span>
	            </div>

	            <p class="desc" id="authRadiusDescription"
	               style="margin-top:14px;">
	                목표 장소에서 300m 이내에 도착하면 GPS 인증이 가능합니다.
	            </p>

	            <button type="button"
	                    class="btn btn-primary"
	                    style="width:100%; margin-top:20px;"
	                    onclick="App.quest.confirmAuthRadius()">
	                이 반경으로 일정 추가하기
	            </button>
	        </div>
	    </div>
	</div>

    <!-- 컨텍스트 메뉴 및 토스트 -->
    <div class="context-menu" id="rowMenu"></div>
    <div class="toast desc" id="toast"></div>
    
</body>
</html>
