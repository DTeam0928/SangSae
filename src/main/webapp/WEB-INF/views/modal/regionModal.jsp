<div class="modal-bg" id="regionModal" onclick="App.quest.closeModalOutside(event, 'regionModal')" aria-hidden="true">
    <div class="modal" role="dialog" aria-modal="true" aria-labelledby="regionModalTitle">
        <div class="modal-head">
            <h2 class="title" id="regionModalTitle">탐색할 지역 선택</h2>
            <button type="button" class="close" aria-label="닫기" onclick="ModalManager.close('regionModal')">×</button>
        </div>
        <div class="region-filter" style="display:grid; grid-template-columns: minmax(120px, 0.8fr) minmax(180px, 1.2fr); gap:16px;">
            <section aria-labelledby="regionSidoTitle">
                <h3 class="subtitle" id="regionSidoTitle">시·도</h3>
                <div id="regionSidoList" role="listbox" aria-label="시·도 선택"></div>
            </section>
            <section aria-labelledby="regionSigunguTitle">
                <h3 class="subtitle" id="regionSigunguTitle">시·군·구</h3>
                <div id="regionSigunguList" role="group" aria-label="시·군·구 선택"></div>
            </section>
        </div>
        <div class="desc" id="selectedRegionSummary" aria-live="polite" style="margin:14px 0;"></div>
        <div style="display:flex; justify-content:flex-end; gap:8px;">
            <button type="button" class="btn btn-secondary" onclick="App.quest.resetRegions()">전국 선택</button>
            <button type="button" class="btn btn-primary" onclick="App.quest.applyRegions()">선택 적용</button>
        </div>
    </div>
</div>
