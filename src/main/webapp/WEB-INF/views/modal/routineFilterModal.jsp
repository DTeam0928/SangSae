<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<div class="modal-bg" id="routineFilterModal" onclick="App.routine.closeModal('routineFilterModal', event)">
    <div class="modal" onclick="event.stopPropagation()">
        <div class="modal-head">
            <h2 class="title">루틴 필터</h2>
            <button type="button" class="close" onclick="ModalManager.close('routineFilterModal')">×</button>
        </div>
        <!-- 동적 생성 대신 직접 폼 형태로 구성 -->
        <div class="ds-p-5">
            <p class="type-sm color-muted ds-mb-3">검색할 주변 루틴 스팟 키워드를 입력하세요<br>(예: 운동, 러닝, 공원, 카페, 도서관, 헬스장)</p>
            <input type="text" id="placeKeywordInput" class="width-full border-default radius-md ds-p-3 ds-mb-4" value="운동" placeholder="키워드 입력">
            <div class="layout-row justify-end ds-gap-2">
                <button type="button" class="btn btn-secondary ds-px-4 ds-py-2" onclick="ModalManager.close('routineFilterModal')">취소</button>
                <button type="button" class="btn btn-primary ds-px-4 ds-py-2" onclick="App.routine.submitPlaceFilter()">검색</button>
            </div>
        </div>
    </div>
</div>