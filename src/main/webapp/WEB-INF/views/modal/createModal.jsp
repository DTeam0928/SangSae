<!-- 5. 퀘스트 생성 모달 -->
<div class="modal-bg" id="createModal" onclick="App.quest.closeModalOutside(event, 'createModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">✨ 나만의 퀘스트 만들기</h2>
            <button class="close" onclick="App.quest.closeModal('createModal')">×</button>
        </div>
        <div class="quest-detail-3">
            <label class="form-label">퀘스트 제목</label>
            <input id="newTitle" class="form-input" placeholder="예: 광안리 일몰 보기">
            <label class="form-label" for="newDescription">퀘스트 설명</label>
            <textarea id="newDescription" class="form-input" rows="3"
                      placeholder="퀘스트 내용을 설명해 주세요"></textarea>

            <label class="form-label" for="newCategory">카테고리</label>
            <select id="newCategory" class="form-input">
				<option value="PARK">산책</option>
				<option value="CAFE">카페</option>
				<option value="CULTURE">문화</option>
				<option value="GYM">운동</option>
				<option value="EXPLORE">탐방</option>
            </select>

            <label class="form-label" for="newEstimatedMinutes">예상 소요 시간 (분)</label>
            <input id="newEstimatedMinutes" class="form-input"
                   type="number" min="1" value="30">

            <label class="form-label" for="newRewardXp">보상 XP</label>
            <input id="newRewardXp" class="form-input"
                   type="number" min="0" value="100">

            <label class="form-label" for="newAuthType">인증 방식</label>
            <select id="newAuthType" class="form-input"
                    onchange="App.quest.toggleCreatePlaceSearch()">
                <option value="NONE">인증 없음</option>
                <option value="LOCATION">위치 인증 (GPS)</option>
                <option value="PHOTO">사진 인증</option>
                <option value="TEXT">텍스트 인증</option>
            </select>

            <!-- LOCATION 선택 시에만 표시 -->
            <div id="createPlaceSection" style="display:none;">
                <label class="form-label" for="createPlaceQuery">장소 검색</label>
                <div style="display:flex; gap:8px;">
                    <input id="createPlaceQuery" class="form-input"
                           placeholder="예: 부산시민공원">
                    <button type="button" class="btn btn-primary"
                            onclick="App.quest.searchCreatePlaces()">검색</button>
                </div>
                <div id="createPlaceResults"></div>
                <div id="createSelectedPlace"></div>
            </div>
            <label class="form-label" for="newVisibility">공개 범위</label>
            <select id="newVisibility" class="form-input">
                <option value="PUBLIC">전체 공개</option>
                <option value="PRIVATE">비공개</option>
            </select>
        </div>
        <button class="quest-detail-4 btn btn-primary" onclick="App.quest.createQuest()">퀘스트 발행하기</button>
    </div>
</div>