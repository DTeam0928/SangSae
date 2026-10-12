<%@ include file="/WEB-INF/views/common/head.jsp"%>
<%@ include file="/WEB-INF/views/modal/modal_head.jsp"%>
<script src="mypage.js"></script>

<body class="bg-sub-bg">

<%@ include file="/WEB-INF/views/common/header.jsp"%>

<div class="container py-24" style="max-width: 1300px; margin: 0 auto;">
    <h1 class="title mb-24">마이페이지</h1>

    <div class="grid-12">

        <aside class="col-3 surface-white border-default rounded-lg p-24">
            <h3 class="subtitle mb-16">메뉴</h3>
            <ul class="flex flex-col gap-8">
                <li><a href="#" class="color-dark font-bold">계정 설정</a></li>
            </ul>
        </aside>


        <main class="col-9">
            <div class="grid-12">
                <div class="col-6 dash-card flex flex-col justify-between">
                    <div>
                        <div class="dash-card-header">
                            <h3 class="dash-title">닉네임 변경</h3>
                            <span class="badge">계정</span>
                        </div>
                        <p class="desc mb-16">새로운 닉네임을 설정합니다.</p>
                        <input type="text" class="form-input mb-16" placeholder="변경할 닉네임 입력">
                    </div>
                    <button class="btn btn-primary mt-16">변경하기</button>
                </div>

                <div class="col-6 dash-card flex flex-col justify-between">
                    <div>
                        <div class="dash-card-header">
                            <h3 class="dash-title">관심사 변경</h3>
                            <span class="badge orange">취향</span>
                        </div>
                        <p class="desc mb-16">관심 있는 분야를 선택하세요.</p>
                        <div class="chip-grid mb-16">
                            <span class="interest-chip selected">맛집</span>
                            <span class="interest-chip">카페</span>
                            <span class="interest-chip">액티비티</span>
                            <span class="interest-chip">사진</span>
                            <span class="interest-chip">산책/조깅</span>
                            <span class="interest-chip">독서/공부</span>
                            <span class="interest-chip">문화/예술</span>
                            <span class="interest-chip">힐링/자연</span>
                        </div>
                    </div>
                    <button class="btn btn-primary mt-16">저장하기</button>
                </div>

                <div class="col-6 dash-card flex flex-col justify-between">
                    <div>
                        <div class="dash-card-header">
                            <h3 class="dash-title">위치 권한 허용</h3>
                            <span class="badge green">위치</span>
                        </div>
                        <p class="desc mb-16">위치 기반 루틴 및 장소 기능을 사용하려면 권한을 허용해주세요.</p>
                    </div>
                    <button class="btn btn-allow w-full mt-16">권한 허용하기</button>
                </div>

                <div class="col-6 dash-card flex flex-col justify-between">
                    <div>
                        <div class="dash-card-header">
                            <h3 class="dash-title">테마 설정</h3>
                            <span class="badge green">스타일</span>
                        </div>
                        <p class="desc mb-16">앱의 분위기와 색상을 취향에 맞게 바꿔보세요.</p>
                        <div class="flex flex-col gap-8 mb-16">
                            <label class="flex items-center justify-between p-8 surface-sub-bg rounded-md cursor-pointer">
                                <span class="text-sm font-bold">🌿 상새 기본 민트 (Light)</span>
                                <input type="radio" name="appTheme" value="light" checked class="input-accent">
                            </label>
                            <label class="flex items-center justify-between p-8 surface-sub-bg rounded-md cursor-pointer">
                                <span class="text-sm font-bold">🌙 다크 모드 (Dark)</span>
                                <input type="radio" name="appTheme" value="dark" class="input-accent">
                            </label>
                        </div>
                    </div>
                    <button class="btn btn-secondary w-full mt-16" onclick="App.user.applyTheme()">테마 적용하기</button>
                </div>

            </div>
        </main>

    </div>
</div>