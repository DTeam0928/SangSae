<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 퀘스트 상세 보기 모달 (detailModal)
- 퀘스트의 상세 카드 정보(장소, 소요시간, 보상) 확인 및 일정 추가 시 사용합니다.
================================================================== -->
<div class="modal-bg" id="detailModal" onclick="App.quest.closeModalOutside(event, 'detailModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">퀘스트 상세</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('detailModal')">×</button>
        </div>
        <!-- 자바스크립트에 의해 해당 퀘스트의 세부 정보가 동적으로 렌더링됨 -->
        <div id="detailContent"></div>
    </div>
</div>