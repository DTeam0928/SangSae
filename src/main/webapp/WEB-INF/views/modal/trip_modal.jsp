<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 내 여행 목록 선택/전환 모달 (tripModal)
- 여러 여행 일정 중 현재 관리할 여행 항목을 선택 및 전환할 때 사용합니다.
================================================================== -->
<div class="modal-bg" id="tripModal" onclick="App.quest.closeModalOutside(event, 'tripModal')">
    <div class="quest-detail-2 modal">
        <div class="modal-head">
            <h2 class="title">내 여행 목록</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('tripModal')">×</button>
        </div>
        <!-- 자바스크립트에 의해 여행 목록 버튼이 동적으로 생성되는 컨테이너 -->
        <div id="tripListContent"></div>
    </div>
</div>