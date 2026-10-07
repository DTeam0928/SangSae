<%-- 일정에 등록하는 커스텀 퀘스트이므로 user_quests 컬럼을 사용합니다. user_id, id, created_at, completed_at은 서버에서 처리합니다. 빈 선택 입력은 NULL로 정규화합니다. LOCATION일 때 auth_radius_m을 필수로 검증하고 장소/GPS 조건을 확인해야 합니다. 버튼의 required 검증은 향후 JS에서 reportValidity 등으로 실행해야 합니다. 보상 및 상태 값은 서버에서도 검증합니다. --%>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!-- =================================================================
[모달] 커스텀 퀘스트 작성 모달 (createModal)
- 사용자가 직접 나만의 커스텀 퀘스트를 생성하여 일정에 등록할 때 사용합니다.
================================================================== -->
<div class="modal-bg" id="createModal" onclick="App.quest.closeModalOutside(event, 'createModal')">
    <div class="modal">
        <div class="modal-head">
            <h2 class="title">✨ 나만의 퀘스트 만들기</h2>
            <button type="button" class="close" onclick="App.quest.closeModal('createModal')">×</button>
        </div>
        <div class="quest-detail-3">
            <label class="form-label" for="newTitle">퀘스트 제목</label>
            <input id="newTitle" name="title" type="text" required maxlength="150" class="form-input" placeholder="예: 광안리 일몰 보기">
            <label class="form-label" for="newScheduledDate">실천 예정 일자 *</label>
            <input id="newScheduledDate" name="scheduled_date" class="form-input" type="date" required>
            <label class="form-label" for="newTargetTime">목표 수행 시간 (선택)</label>
            <input id="newTargetTime" name="target_time" class="form-input" type="time" step="1">
            <label class="form-label" for="newTripId">소속 여행 ID (선택)</label>
            <input id="newTripId" name="trip_id" class="form-input" type="text" inputmode="numeric" pattern="[1-9][0-9]{0,18}" maxlength="19" placeholder="trips.id — 미입력 시 여행 없음">
            <label class="form-label" for="newPlaceId">실천 장소 ID (선택)</label>
            <input id="newPlaceId" name="place_id" class="form-input" type="text" inputmode="numeric" pattern="[1-9][0-9]{0,18}" maxlength="19" placeholder="places.id — 미입력 시 장소 없음">
            <label class="form-label" for="newAuthType">인증 방식 *</label>
            <select id="newAuthType" name="auth_type" class="form-input" required>
                <option value="NONE" selected>인증 없음</option>
                <option value="PHOTO">사진 인증</option>
                <option value="LOCATION">위치 인증</option>
                <option value="TEXT">텍스트 인증</option>
            </select>
            <label class="form-label" for="newAuthRadius">위치 인증 허용 반경 (m, 위치 인증 시 필수)</label>
            <input id="newAuthRadius" name="auth_radius_m" class="form-input" type="number" min="100" max="1000" step="1" placeholder="100~1000">
            <label class="form-label" for="newStatus">퀘스트 상태 *</label>
            <select id="newStatus" name="status" class="form-input" required>
                <option value="TODO" selected>수행 예정</option>
                <option value="COMPLETED">완료</option>
                <option value="CANCELED">취소</option>
            </select>
            <label class="form-label" for="newRewardXp">완료 보상 XP *</label>
            <input id="newRewardXp" name="reward_xp" class="form-input" type="number" required min="0" max="2147483647" step="1" value="100">
            <label class="form-label" for="newMemo">개인 메모 (선택)</label>
            <textarea id="newMemo" name="memo" class="form-input" maxlength="255" rows="3" placeholder="255자 이내 개인 메모"></textarea>
        </div>
        <button type="button" class="quest-detail-4 btn btn-primary" onclick="App.quest.createQuest()">퀘스트 발행하기</button>
    </div>
</div>