<%-- 루틴 기본 정보는 user_routines, 기본 Task 입력은 user_routine_tasks에 각각 매핑합니다. rfDesc는 루틴 설명 컬럼이 없으므로 Task memo로 변경합니다. rfRepeat 역시 Task별 repeat_type의 기본값입니다. CUSTOM 요일은 routine_task_schedules.day_of_week(MON~SUN)에 별도 저장해야 하며 이 JSP에는 요일 UI를 추가하지 않습니다. 빈 end_date / alert_time은 NULL로 정규화하고 날짜 순서는 JS/API에서 검증합니다. 기존 Task 수정 범위 및 CUSTOM 요일 선택은 후속 JS/API에서 처리합니다. --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 새 루틴 생성 및 기본 정보 수정 모달 (routineFormModal)
- 새로운 루틴 그룹의 이름, 설명, 운영 기간 및 기본 반복 옵션을 입력받을 때 사용합니다.
================================================================== -->
<div id="routineFormModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('routineFormModal',event)">
    <div class="surface-white radius-lg width-full max-width-lg ds-p-6" onclick="event.stopPropagation()">
        <div class="layout-row justify-between align-center ds-mb-5">
            <h3 id="rfTitle" class="type-xl weight-heading">새 루틴 만들기</h3>
            <button type="button" onclick="App.routine.hide('routineFormModal')" class="type-2xl color-muted">×</button>
        </div>

        <form onsubmit="App.routine.saveRoutineInfo(event)" class="space-y-4">
            <div>
                <label class="type-xs weight-bold" for="rfName">루틴 이름 *</label>
                <input id="rfName" name="title" type="text" required maxlength="100" placeholder="루틴 이름 (100자 이내)" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
            </div>
            <div>
                <label class="type-xs weight-bold" for="rfDesc">기본 Task 개인 메모 (선택)</label>
                <textarea id="rfDesc" name="memo" maxlength="255" placeholder="기본 Task에 적용할 메모 (255자 이내)" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" rows="3"></textarea>
            </div>
            <div>
                <label class="type-xs weight-bold">루틴 기간 (시작일 필수, 종료일 선택)</label>
                <div class="layout-grid columns-2 ds-gap-2 ds-mt-1">
                    <input id="rfStart" name="start_date" type="date" required aria-label="운영 시작 일자" class="border-default radius-md ds-px-3 ds-py-3">
                    <input id="rfEnd" name="end_date" type="date" aria-label="운영 종료 일자 (미입력 시 무기한)" class="border-default radius-md ds-px-3 ds-py-3">
                </div>
            </div>
            <div>
                <label class="type-xs weight-bold" for="rfRepeat">기본 Task 반복 유형 *</label>
                <select id="rfRepeat" name="repeat_type" required class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                    <option value="DAILY" selected>매일</option>
                    <option value="WEEKDAY">평일</option>
                    <option value="WEEKEND">주말</option>
                    <option value="CUSTOM">사용자 지정</option>
                    <option value="ONCE">한 번만</option>
                </select>
                <label class="type-xs weight-bold" for="rfTaskContent">기본 Task 내용 *</label>
                <input id="rfTaskContent" name="content" type="text" required maxlength="150" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" placeholder="실천할 Task 내용 (150자 이내)">
                <label class="type-xs weight-bold" for="rfCategory">기본 Task 카테고리 코드 *</label>
                <input id="rfCategory" name="category" type="text" required maxlength="50" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" placeholder="등록된 카테고리 코드">
                <label class="type-xs weight-bold" for="rfTargetTime">기본 Task 실천 시간 *</label>
                <input id="rfTargetTime" name="target_time" type="time" required step="1" value="07:00:00" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                <label class="type-xs weight-bold" for="rfAlertTime">기본 Task 알림 시간 (선택)</label>
                <input id="rfAlertTime" name="alert_time" type="time" step="1" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                <label class="type-xs weight-bold" for="rfActive">루틴 활성 여부 *</label>
                <select id="rfActive" name="is_active" required class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                    <option value="1" selected>활성</option>
                    <option value="0">비활성</option>
                </select>
            </div>
            <div class="layout-row ds-gap-2">
                <button type="submit" class="layout-grow surface-dark color-white ds-py-3 radius-md weight-bold">저장</button>
                <button type="button" onclick="App.routine.hide('routineFormModal')" class="ds-px-5 surface-sub-bg radius-md weight-bold">취소</button>
            </div>
        </form>
    </div>
</div>