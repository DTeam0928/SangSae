<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 루틴 상세 및 Task 선택 모달 (routineModal)
- 추천 루틴의 적합도, 장소, 포함된 Task 목록을 확인하고 내 일에 추가할 때 사용합니다.
================================================================== -->
<div id="routineModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('routineModal',event)">
    <div class="surface-white radius-lg width-full max-width-2xl ds-max-h-92vh clip-overflow" onclick="event.stopPropagation()">
        <!-- 모달 헤더 -->
        <div class="ds-p-6 border-bottom layout-row justify-between">
            <div>
                <div id="rmBadges" class="layout-row ds-gap-2 ds-mb-2"></div>
                <h3 id="rmTitle" class="type-2xl weight-heading"></h3>
                <p id="rmDesc" class="type-sm color-muted ds-mt-1"></p>
            </div>
            <button type="button" onclick="App.routine.hide('routineModal')" class="type-2xl color-muted">×</button>
        </div>

        <!-- 모달 바디: 세부 통계 정보 및 포함 Task 리스트 -->
        <div class="ds-p-6 scroll-y ds-max-h-64vh surface-sub-bg">
            <div class="layout-grid columns-1 medium-columns-3 ds-gap-3 ds-mb-4">
                <div class="surface-white radius-md ds-p-3 border-default">
                    <div class="type-10px color-muted">추천 장소</div>
                    <b id="rmPlace"></b>
                </div>
                <div class="surface-white radius-md ds-p-3 border-default">
                    <div class="type-10px color-muted">장소 적합도</div>
                    <b id="rmFit"></b>
                </div>
                <div class="surface-white radius-md ds-p-3 border-default">
                    <div class="type-10px color-muted">주변 수행자</div>
                    <b id="rmUsers"></b>
                </div>
            </div>

            <div class="layout-row justify-between ds-mb-3">
                <b>포함 Task</b>
                <label class="type-xs"><input id="rmAll" type="checkbox" checked onchange="App.routine.toggleModalTasks(this)"> 전체 선택</label>
            </div>
            <div id="rmTasks" class="space-y-2"></div>
        </div>

        <!-- 모달 푸터 액션 버튼 -->
        <div class="ds-p-5 border-top layout-row ds-gap-2">
            <button type="button" onclick="App.routine.addSelectedRoutineTasks()" class="layout-grow ds-py-3 border-default radius-md weight-bold">선택 Task 담기</button>
            <button type="button" onclick="App.routine.addAllRoutineTasks()" class="layout-grow ds-py-3 surface-dark color-white radius-md weight-bold">전체 루틴 담기</button>
        </div>
    </div>
</div>