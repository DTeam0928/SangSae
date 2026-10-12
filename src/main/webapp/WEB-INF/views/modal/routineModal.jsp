<div id="routineModal" class="modal-bg position-fixed inset-zero surface-overlay ds-z-50 align-center justify-center ds-p-4" onclick="App.routine.closeModal('routineModal',event)">
    <div class="surface-white radius-lg width-full max-width-2xl ds-max-h-92vh clip-overflow" onclick="event.stopPropagation()">
        <div class="ds-p-6 border-bottom layout-row justify-between">
            <div>
                <div id="rmBadges" class="layout-row ds-gap-2 ds-mb-2"></div>
                <h3 id="rmTitle" class="type-2xl weight-heading"></h3>
                <p id="rmDesc" class="type-sm color-muted ds-mt-1"></p>
            </div>
            <button type="button" onclick="App.routine.hide('routineModal')" class="type-2xl color-muted">×</button>
        </div>
        <div class="ds-p-6 scroll-y ds-max-h-64vh surface-sub-bg">
            <div class="layout-grid columns-1 medium-columns-3 ds-gap-3 ds-mb-4">
                <div class="surface-white radius-md ds-p-3 border-default"><div class="type-10px color-muted">추천 장소</div><b id="rmPlace"></b></div>
                <div class="surface-white radius-md ds-p-3 border-default"><div class="type-10px color-muted">관심사 적합도</div><b id="rmFit"></b></div>
                <div class="surface-white radius-md ds-p-3 border-default"><div class="type-10px color-muted">주변 수행자</div><b id="rmUsers"></b></div>
            </div>
            <div class="layout-row justify-between ds-mb-3">
                <b>포함 Task</b>
            </div>
            <div id="rmTasks" class="space-y-2"></div>
        </div>
        <div class="ds-p-5 border-top layout-row ds-gap-2">
            <button type="button" id="rmAdoptBtn" onclick="App.routine.addAllRoutineTasks()" class="layout-grow ds-py-3 surface-dark color-white radius-md weight-bold">내 루틴에 담기</button>
        </div>
    </div></div>

<!-- Place Detail Modal -->
