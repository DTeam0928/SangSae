<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 탐색 지역 선택 모달 (regionModal)
- 사용자가 퀘스트/루틴 탐색 시 대상 지역(예: 부산)을 선택할 때 사용합니다.
================================================================== -->
<div class="modal-bg" id="regionModal" onclick="App.quest.closeModalOutside(event, 'regionModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">탐색할 지역 선택</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('regionModal')">×</button>
        </div>
        <div class="quest-detail-1">
            <p class="desc">부산 지역 필터가 기본 적용되어 있습니다.</p>
            <button type="button" class="btn btn-primary" onclick="App.quest.applyRegions()">부산 전체 적용</button>
        </div>
    </div>
</div>