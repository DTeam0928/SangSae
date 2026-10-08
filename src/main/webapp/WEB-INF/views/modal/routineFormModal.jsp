<div id="routineFormModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('routineFormModal',event)">
    <div class="surface-white radius-lg width-full max-width-lg ds-p-6" onclick="event.stopPropagation()">
        <div class="layout-row justify-between align-center ds-mb-5">
            <h3 id="rfTitle" class="type-xl weight-heading">새 루틴 만들기</h3>
            <button type="button" onclick="App.routine.hide('routineFormModal')" class="type-2xl color-muted">×</button>
        </div>
        <form onsubmit="App.routine.saveRoutineInfo(event)" class="space-y-4">
            <div><label class="type-xs weight-bold" for="rfName">루틴 이름 *</label><input id="rfName" required maxlength="50" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3"></div>
            <div><label class="type-xs weight-bold" for="rfDesc">설명</label><textarea id="rfDesc" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" rows="3"></textarea></div>
            <div>
                <label class="type-xs weight-bold">루틴 기간 *</label>
                <div class="layout-grid columns-2 ds-gap-2 ds-mt-1">
                    <input id="rfStart" type="date" required class="border-default radius-md ds-px-3 ds-py-3">
                    <input id="rfEnd" type="date" required class="border-default radius-md ds-px-3 ds-py-3">
                </div>
            </div>
            <div>
                <label class="type-xs weight-bold" for="rfRepeat">공개 설정</label>
                <select id="rfRepeat" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                    <option>공개</option><option>비공개</option>
                </select>
            </div>
            <div>
                <label class="type-xs weight-bold" for="rfRepeat">기본 반복</label>
                <select id="rfRepeat" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3">
                    <option>매일</option><option>평일</option><option>주말</option><option>사용자 지정</option>
                </select>
            </div>
            <div class="layout-row ds-gap-2">
                <button type="submit" class="layout-grow surface-dark color-white ds-py-3 radius-md weight-bold">저장</button>
                <button type="button" onclick="App.routine.hide('routineFormModal')" class="ds-px-5 surface-sub-bg radius-md weight-bold">취소</button>
            </div>
        </form>
    </div></div>
