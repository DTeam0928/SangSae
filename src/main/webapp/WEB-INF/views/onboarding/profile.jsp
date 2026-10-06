<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>상새</title>
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/css/onboarding.css">
<script src="${pageContext.request.contextPath}/assets/js/onboarding.js" defer></script>
</head>
<body data-page="onboarding" data-context-path="${pageContext.request.contextPath}">
<main id="onboarding-viewport"><section class="step-section active">


<form id="profile-form" class="profile-container" method="POST" action="${pageContext.request.contextPath}/onboarding/profile" onsubmit="return App.onboarding.handleSubmitProfile(event)">
    <div class="step-indicator">Step 1 of 2</div>
    <div class="title">프로필 및 관심사 설정</div>
    <div class="desc">상새에서 사용할 닉네임과 선호하는 여행/습관 테마를 선택해주세요.</div>

    <!-- 닉네임 설정 영역 -->
    <div class="form-group">
        <label class="form-label" for="nickname">닉네임 <span>(1~15자)</span></label>
        <input type="text" id="nickname" name="nickname" required class="form-input" placeholder="사용할 닉네임을 입력해주세요" maxlength="15" oninput="App.onboarding.hideNicknameError()">
        <div class="error-msg" id="nickname-error">닉네임을 1~15자로 올바르게 입력해 주세요.</div>
    </div>

    <!-- 관심사 테마 선택 영역 -->
    <div class="form-group">
        <label class="form-label">관심사 테마 <span>(최대 5개 선택)</span></label>
        <div class="chip-grid" id="chip-grid">
            <label class="interest-chip"><input type="checkbox" name="interests" value="맛집" onchange="App.onboarding.toggleChip(this)">#맛집</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="카페" onchange="App.onboarding.toggleChip(this)">#카페</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="액티비티" onchange="App.onboarding.toggleChip(this)">#액티비티</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="사진" onchange="App.onboarding.toggleChip(this)">#사진</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="산책/조깅" onchange="App.onboarding.toggleChip(this)">#산책/조깅</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="독서/공부" onchange="App.onboarding.toggleChip(this)">#독서/공부</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="문화/예술" onchange="App.onboarding.toggleChip(this)">#문화/예술</label>
            <label class="interest-chip"><input type="checkbox" name="interests" value="힐링/자연" onchange="App.onboarding.toggleChip(this)">#힐링/자연</label>
        </div>
        <div class="error-msg" id="chip-error">최소 1개의 관심사를 선택해주세요.</div>
        <div class="error-msg" id="chip-error2">최대 5개까지 선택할 수 있어요.</div>
    </div>

    <!-- 하단 액션 버튼 -->
    <div class="btn-group">
        <button type="submit" class="btn btn-primary">다음</button>
    </div>
</form>

</section></main>
</body></html>
