<%-- user_quest_bookmarks 기반 조회 컨테이너. 정적 입력 필드가 없어 구조를 유지합니다. folder_name은 동적 렌더링 단계에서 maxlength=50으로 처리합니다. --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 내 컬렉션 보관함 모달 (collectionModal)
- 사용자가 저장(좋아요)해둔 퀘스트 목록을 조회하고 일정에 추가할 때 사용합니다.
================================================================== -->
<div class="modal-bg" id="collectionModal" onclick="App.quest.closeModalOutside(event, 'collectionModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">♡ 내 컬렉션</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('collectionModal')">×</button>
        </div>
        <!-- 자바스크립트(App.quest.openCollectionModal)로 저장된 퀘스트 동적 주입 -->
        <div id="collectionBody"></div>
    </div>
</div>