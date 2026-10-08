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
