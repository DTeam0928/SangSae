<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 루틴 스팟/장소 상세 모달 (placeModal)
- 선택한 장소(헬스장, 카페 등)의 주소, 거리, 인기 있는 루틴 목록을 확인할 때 사용합니다.
================================================================== -->
<div id="placeModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('placeModal',event)">
    <div class="surface-white radius-lg width-full max-width-xl ds-max-h-90vh clip-overflow" onclick="event.stopPropagation()">
        <!-- 모달 헤더 -->
        <div class="ds-p-6 border-bottom layout-row justify-between">
            <div>
                <div class="type-xs weight-bold color-dark" id="pmType"></div>
                <h3 class="type-2xl weight-heading ds-mt-1" id="pmName"></h3>
                <p class="type-sm color-muted ds-mt-1" id="pmAddress"></p>
            </div>
            <button type="button" onclick="App.routine.hide('placeModal')" class="type-2xl color-muted">×</button>
        </div>

        <!-- 모달 바디: 거리/핫도 및 이 장소의 인기 루틴 목록 -->
        <div class="ds-p-6 scroll-y ds-max-h-60vh">
            <div class="layout-grid columns-3 ds-gap-3 ds-mb-5">
                <div class="surface-sub-bg radius-md ds-p-3"><div class="type-10px color-muted">거리</div><b id="pmDistance"></b></div>
                <div class="surface-sub-bg radius-md ds-p-3"><div class="type-10px color-muted">루틴 수</div><b id="pmRoutineCount"></b></div>
                <div class="surface-sub-bg radius-md ds-p-3"><div class="type-10px color-muted">핫도</div><b id="pmHot"></b></div>
            </div>
            <h4 class="weight-heading ds-mb-3">이 장소에서 인기 있는 루틴</h4>
            <div id="pmRoutines" class="space-y-3"></div>
        </div>

        <!-- 하단 확인 버튼 -->
        <div class="ds-p-5 border-top">
            <button type="button" onclick="App.routine.hide('placeModal')" class="width-full ds-py-3 surface-dark color-white radius-md weight-bold">확인</button>
        </div>
    </div>
</div>