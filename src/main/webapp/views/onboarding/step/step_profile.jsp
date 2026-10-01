<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<head>
    <title>onboarding</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/views/style-jsp.css">
    <style>
    /* ===== Step 3 (프로필 설정) 고유 스타일 ===== */
    .profile-container {
        width: min(440px, calc(100% - 40px));
        margin: 0 auto;
        background: var(--color-white);
        padding: 40px 30px;
        border-radius: var(--radius-lg);
        box-shadow: var(--shadow-card);
        border: 1px solid var(--color-border);
    }

    .profile-container .step-indicator {
        font-size: var(--fs-small);
        font-weight: 700;
        color: var(--color-primary);
        margin-bottom: 8px;
        text-transform: uppercase;
        letter-spacing: 0.05em;
    }

    .profile-container .title {
        font-size: var(--fs-h1);
        font-weight: var(--fw-h1);
        margin-bottom: 6px;
    }

    .profile-container .desc {
        font-size: var(--fs-body);
        color: var(--color-muted);
        margin-bottom: 28px;
    }

    .profile-container .form-group {
        margin-bottom: 24px;
    }

    .profile-container .form-label {
        display: flex;
        justify-content: space-between;
        align-items: center;
    }

    .profile-container .form-label span {
        font-weight: 400;
        color: var(--color-muted);
    }

    /* 관심사 칩 그리드 */
    .chip-grid {
        display: flex;
        flex-wrap: wrap;
        gap: 8px;
    }

    .interest-chip {
        background: var(--color-sub-bg);
        border: 1px solid var(--color-border);
        padding: 8px 14px;
        border-radius: 20px;
        font-size: 13px;
        font-weight: 600;
        color: var(--color-text);
        cursor: pointer;
        transition: border-color 0.2s, background-color 0.2s;
        user-select: none;
    }

    .interest-chip:hover {
        border-color: #b9e3ff;
    }

    .interest-chip.selected {
        background: var(--color-main);
        color: var(--color-primary);
        border-color: var(--color-primary);
        font-weight: 700;
    }

    .profile-container .error-msg {
        display: none;
        font-size: var(--fs-small);
        font-weight: 600;
        color: var(--color-danger);
        background-color: var(--color-danger-bg);
        padding: 8px 12px;
        border-radius: var(--radius-sm);
        margin-top: 8px;
        animation: fadeIn 0.2s ease-in-out;
    }

    .profile-container .btn-group {
        display: flex;
        gap: 10px;
        margin-top: 32px;
    }

    .profile-container .btn-group .btn {
        flex: 1;
        height: 48px;
        margin-top: 0;
        width: auto;
        transition: background-color 0.2s;
        cursor: pointer;
    }

    .btn-skip {
        background: var(--color-white);
        border: 1px solid var(--color-border);
        color: var(--color-muted);
    }

    .btn-skip:hover {
        background: #f8fafc;
    }

    .btn-primary {
        background-color: var(--color-primary);
        border: none;
        color: var(--color-white);
    }

    .btn-primary:hover {
        background-color: var(--color-dark);
    }
    </style>
</head>


<div class="profile-container">
    <div class="step-indicator">Step 1 of 2</div>
    <div class="title">프로필 및 관심사 설정</div>
    <div class="desc">QUESTRIP에서 사용할 닉네임과 선호하는 여행/습관 테마를 선택해주세요.</div>

    <!-- 닉네임 설정 영역 -->
    <div class="form-group">
        <label class="form-label" for="nickname">닉네임 <span>(1~15자)</span></label>
        <input type="text" id="nickname" class="form-input" placeholder="사용할 닉네임을 입력해주세요" maxlength="15" oninput="hideNicknameError()">
        <div class="error-msg" id="nickname-error">닉네임을 1~15자로 올바르게 입력해 주세요.</div>
    </div>

    <!-- 관심사 테마 선택 영역 -->
    <div class="form-group">
        <label class="form-label">관심사 테마 <span>(최대 5개 선택)</span></label>
        <div class="chip-grid" id="chip-grid">
            <div class="interest-chip" onclick="toggleChip(this)">#맛집</div>
            <div class="interest-chip" onclick="toggleChip(this)">#카페</div>
            <div class="interest-chip" onclick="toggleChip(this)">#액티비티</div>
            <div class="interest-chip" onclick="toggleChip(this)">#사진</div>
            <div class="interest-chip" onclick="toggleChip(this)">#산책/조깅</div>
            <div class="interest-chip" onclick="toggleChip(this)">#독서/공부</div>
            <div class="interest-chip" onclick="toggleChip(this)">#문화/예술</div>
            <div class="interest-chip" onclick="toggleChip(this)">#힐링/자연</div>
        </div>
        <div class="error-msg" id="chip-error">최대 5개까지 선택할 수 있어요.</div>
    </div>

    <!-- 하단 액션 버튼 -->
    <div class="btn-group">
        <button type="button" class="btn btn-skip" onclick="navigateToStep(4)">건너뛰기</button>
        <button type="button" class="btn btn-primary" onclick="handleSubmitProfile()">다음</button>
    </div>
</div>

<script>
    function toggleChip(element) {
        const selectedChips = document.querySelectorAll('.interest-chip.selected');
        const chipError = document.getElementById('chip-error');

        if (element.classList.contains('selected')) {
            element.classList.remove('selected');
            chipError.style.display = 'none';
        } else {
            if (selectedChips.length >= 5) {
                chipError.style.display = 'block';
                return;
            }
            element.classList.add('selected');
            chipError.style.display = 'none';
        }
    }

    function hideNicknameError() {
        const nicknameInput = document.getElementById('nickname');
        const nicknameError = document.getElementById('nickname-error');
        if (nicknameInput.value.trim().length > 0) {
            nicknameError.style.display = 'none';
        }
    }

    function handleSubmitProfile() {
        const nicknameInput = document.getElementById('nickname');
        const nicknameError = document.getElementById('nickname-error');
        const trimmedVal = nicknameInput.value.trim();

        if (trimmedVal.length === 0 || trimmedVal.length > 15) {
            nicknameError.style.display = 'block';
            nicknameInput.focus();
            return;
        } else {
            nicknameError.style.display = 'none';
        }

        navigateToStep(4);
    }
</script>