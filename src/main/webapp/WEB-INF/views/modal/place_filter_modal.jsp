<%-- 장소 유형은 places.category / place_categories.code 기준으로 동적 구성합니다. 검색 반경(km)은 조회 조건이며 GPS 인증 허용 반경 컬럼과 다릅니다. --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 주변 장소/스팟 탐색 필터 모달 (filterModal)
- 위치 기반 루틴 탐색 시 검색 반경(km) 및 장소 유형 태그를 필터링할 때 사용합니다.
================================================================== -->
<div id="filterModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('filterModal',event)">
    <div class="surface-white radius-lg width-full max-width-lg ds-p-6" onclick="event.stopPropagation()">
        <div class="layout-row justify-between align-center">
            <h3 class="type-xl weight-heading">주변 장소 필터</h3>
            <button type="button" onclick="App.routine.hide('filterModal')" class="type-2xl color-muted">×</button>
        </div>

        <div class="space-y-4 ds-mt-5">
            <!-- 장소 카테고리 태그 칩 -->
            <div>
                <label class="type-xs weight-bold">장소 유형</label>
                <div id="placeTypeFilters" class="layout-row layout-wrap ds-gap-2 ds-mt-2"></div>
            </div>

            <!-- 거리 반경 옵션 -->
            <div>
                <label class="type-xs weight-bold">반경</label>
                <div class="layout-row ds-gap-2 ds-mt-2">
                    <button type="button" class="day-chip" data-radius="1" onclick="App.routine.setRadius(1,this)">1km</button>
                    <button type="button" class="day-chip active" data-radius="3" onclick="App.routine.setRadius(3,this)">3km</button>
                    <button type="button" class="day-chip" data-radius="5" onclick="App.routine.setRadius(5,this)">5km</button>
                    <button type="button" class="day-chip" data-radius="10" onclick="App.routine.setRadius(10,this)">10km</button>
                </div>
            </div>
        </div>

        <button type="button" onclick="App.routine.applyPlaceFilters()" class="width-full ds-mt-6 ds-py-3 surface-dark color-white radius-md weight-bold">적용</button>
    </div>
</div>