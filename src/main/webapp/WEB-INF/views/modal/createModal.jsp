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