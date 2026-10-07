<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 커스텀 퀘스트 작성 모달 (createModal)
- 사용자가 직접 나만의 커스텀 퀘스트를 생성하여 일정에 등록할 때 사용합니다.
================================================================== -->
<div class="modal-bg" id="createModal" onclick="App.quest.closeModalOutside(event, 'createModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">✨ 나만의 퀘스트 만들기</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('createModal')">×</button>
        </div>
        <div class="quest-detail-3">
            <label class="form-label" for="newTitle">퀘스트 제목</label>
            <input id="newTitle" class="form-input" placeholder="예: 광안리 일몰 보기">
        </div>
        <button type="button" class="quest-detail-4 btn btn-primary" onclick="App.quest.createQuest()">퀘스트 발행하기</button>
    </div>
</div>