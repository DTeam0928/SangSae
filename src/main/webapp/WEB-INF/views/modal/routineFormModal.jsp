<div id="routineFormModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('routineFormModal',event)">
    <div class="surface-white radius-lg width-full max-width-lg ds-p-6" onclick="event.stopPropagation()">
        <div class="layout-row justify-between align-center ds-mb-5">
            <h3 id="rfTitle" class="type-xl weight-heading">새 루틴 만들기</h3>
            <button type="button" onclick="App.routine.hide('routineFormModal')" class="type-2xl color-muted">×</button>
        </div>
        <form onsubmit="App.routine.saveRoutineInfo(event)" class="space-y-4">
            <input type="hidden" id="rfRoutineId">
            <div><label class="type-xs weight-bold" for="rfName">루틴 이름 *</label><input id="rfName" required maxlength="100" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3"></div>
            <div>
                <label class="type-xs weight-bold">루틴 기간 * <span class="color-muted">(종료일은 비워두면 무기한)</span></label>
                <div class="layout-grid columns-2 ds-gap-2 ds-mt-1">
                    <input id="rfStart" type="date" required class="border-default radius-md ds-px-3 ds-py-3">
                    <input id="rfEnd" type="date" class="border-default radius-md ds-px-3 ds-py-3">
                </div>
            </div>
            <div>
                <label class="type-xs weight-bold">공개 설정</label>
                <div class="layout-row ds-gap-4 ds-mt-2 type-sm">
                    <label><input type="radio" name="rfVisibility" value="PRIVATE" checked onchange="App.routine.togglePublicFields()"> 비공개</label>
                    <label><input type="radio" name="rfVisibility" value="PUBLIC" onchange="App.routine.togglePublicFields()"> 공개 (탐색 탭에 노출)</label>
                </div>
                <p id="rfAdoptedNote" class="type-xs color-muted ds-mt-1 is-hidden">탐색 탭에서 담은 루틴은 공개할 수 없어요.</p>
            </div>
            <div id="rfPublicFields" class="space-y-4 is-hidden">
                <div><label class="type-xs weight-bold" for="rfDesc">설명</label><textarea id="rfDesc" maxlength="500" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" rows="3"></textarea></div>
                <div>
                    <label class="type-xs weight-bold">카테고리 * <span class="color-muted">(1~3개)</span></label>
                    <div id="rfCategories" class="layout-row layout-wrap ds-gap-3 ds-mt-2 type-sm"></div>
                </div>
                <div>
                    <label class="type-xs weight-bold" for="rfTags">태그 <span class="color-muted">(쉼표로 구분, 최대 5개)</span></label>
                    <input id="rfTags" class="width-full ds-mt-1 border-default radius-md ds-px-3 ds-py-3" placeholder="예: 아침, 초보, 퇴근 운동">
                </div>
            </div>
            <div class="layout-row ds-gap-2">
                <button type="submit" class="layout-grow surface-dark color-white ds-py-3 radius-md weight-bold">저장</button>
                <button type="button" id="rfDeleteBtn" onclick="App.routine.deleteRoutine()" class="ds-px-4 border-default border-color-danger-bg color-danger radius-md is-hidden">루틴 삭제</button>
                <button type="button" onclick="App.routine.hide('routineFormModal')" class="ds-px-5 surface-sub-bg radius-md weight-bold">취소</button>
            </div>
        </form>
    </div></div>