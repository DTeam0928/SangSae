<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="ko">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>일상을 새롭게, 상새 - 종합 대시보드</title>

<!-- 공통 디자인 시스템 CSS -->
<link rel="stylesheet" href="${pageContext.request.contextPath}/style-jsp.css">

<style>
/* =========================================================
   H-00 대시보드 페이지 전용 스타일
   ========================================================= */

body {
    background-color: var(--color-bg);
}

/* 대시보드 2x2 그리드 */
.dashboard-grid {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    gap: 20px;
    margin-bottom: 24px;
}

.extra-section {
    margin-top: 36px;
}

/* 여행 및 루틴 리스트 컨테이너 */
.list-container {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-top: 10px;
}

/* 개별 아이템 행 */
.item-row {
    display: flex;
    align-items: center;
    justify-content: space-between;
    background: var(--color-sub-bg);
    padding: 10px 12px;
    border-radius: 10px;
}

.item-left-group {
    display: flex;
    gap: 10px;
    align-items: center;
}

.item-number {
    font-size: 13px;
    font-weight: 700;
    color: var(--color-muted);
}

.item-subtext {
    margin-top: 2px;
}

.item-btn-group {
    display: flex;
    gap: 4px;
}

/* 셀렉트 형태의 커스텀 뱃지 */
.select-badge {
    border: none;
    background: transparent;
}

/* 카드 내부 플렉스 레이아웃 (추천 퀘스트/커뮤니티 카드) */
.card-flex-col {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
    height: 100%;
}

.card-header-flex {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 10px;
}

.card-title-gap {
    margin-bottom: 6px;
}

/* 통계 위젯 상세 스타일 */
.stat-divider {
    border-left: 1px solid var(--color-border);
    padding-left: 20px;
}

.stat-num-accent {
    color: var(--color-accent);
}

.text-accent-bold {
    color: var(--color-accent);
    font-weight: 800;
}

/* 버튼 전용 컬러 커스텀 */
.btn-community {
    background: #4A5568;
}
.btn-community:hover {
    background: #374151;
}

/* 반응형 설정 */
@media (max-width: 760px) {
    .dashboard-grid {
        grid-template-columns: 1fr;
    }
}
</style>
</head>
<body>

    <!-- 공통 헤더 포함 -->
    <%@ include file="/views/common/header.jsp" %>

<main class="container">
    <div class="section-title">
        <span class="title" onclick="toggleEmptyState()">${nickname}님의 종합 대시보드</span>
    </div>

    <div class="dashboard-grid">
        <!-- 1. 내 여행 관리 -->
        <div class="dash-card">
            <div>
                <div class="dash-card-header">
                    <div class="dash-title">✈️ 내 여행 관리</div>
                    <select class="badge select-badge" id="trip-filter">
                        <option>진행 중 여행</option>
                        <option>예정된 여행</option>
                        <option>완료된 여행</option>
                    </select>
                </div>

                <!-- 여행 목록 또는 Empty 상태 -->
                <div id="trip-content" class="list-container">
                    <div class="item-row">
                        <div class="item-left-group">
                            <span class="item-number">1</span>
                            <div>
                                <div class="subtitle">${trip.title}</div>
                                <div class="desc item-subtext">${trip.start_date} - ${trip.end_date}</div>
                            </div>
                        </div>
                        <div class="item-btn-group">
                            <!-- Q-00 여행 관리 탭으로 이동 -->
                            <button type="button" class="small-btn" onclick="location.href='${pageContext.request.contextPath}/quest/quest_main.jsp#management'">수정</button>
                            <button type="button" class="small-btn danger" onclick="openDeleteModal('여행', '부산 여행')">삭제</button>
                        </div>
                    </div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/quest/quest_main.jsp" class="detail-btn">여행 및 퀘스트 추가하기</a>
        </div>

        <!-- 2. 내 루틴 관리 -->
        <div class="dash-card">
            <div>
                <div class="dash-card-header">
                    <div class="dash-title">🔄 내 루틴 관리</div>
                    <select class="badge orange select-badge" id="routine-filter">
                        <option>활성 루틴</option>
                        <option>비활성 루틴</option>
                    </select>
                </div>

                <!-- 루틴 목록 또는 Empty 상태 -->
                <div id="routine-content" class="list-container">
                    <div class="item-row">
                        <div class="item-left-group">
                            <span class="item-number">1</span>
                            <div>
                                <div class="subtitle">${user_routines.title}</div>
                                <div class="desc item-subtext">${user_routines.start_date} - ${user_routines.end_date}</div>
                            </div>
                        </div>
                        <div class="item-btn-group">
                            <!-- R-00 루틴 관리 탭으로 이동 -->
                            <button type="button" class="small-btn" onclick="location.href='${pageContext.request.contextPath}/routine/routine_main.jsp#manage'">수정</button>
                            <button type="button" class="small-btn danger" onclick="openDeleteModal('루틴', '갓생 살기 루틴')">삭제</button>
                        </div>
                    </div>
                </div>
            </div>
            <a href="${pageContext.request.contextPath}/routine/routine_main.jsp" class="detail-btn">루틴 추가하기</a>
        </div>

        <!-- 3. 내 퀘스트 달성도 -->
        <div class="dash-card">
            <div class="dash-title">🎯 내 퀘스트 달성도</div>
            <div class="stats-row">
                <div>
                    <div class="stat-label">달성한 퀘스트</div>
                    <div class="stat-num">8개</div>
                </div>
                <div class="stat-divider">
                    <div class="stat-label">남은 퀘스트</div>
                    <div class="stat-num stat-num-accent">4개</div>
                </div>
            </div>
        </div>

        <!-- 4. 내 루틴 달성도 -->
        <div class="dash-card">
            <div class="dash-title">🔥 내 루틴 달성도</div>
            <div class="stats-row">
                <div>
                    <div class="stat-label">연속 달성</div>
                    <div class="stat-num stat-num-accent">3일</div>
                </div>
                <div>
                    <div class="stat-label">달성한 할 일</div>
                    <div class="stat-num">15개</div>
                </div>
                <div>
                    <div class="stat-label">남은 할 일</div>
                    <div class="stat-num">2개</div>
                </div>
            </div>
        </div>
    </div>

    <!-- 추가 섹션: 오늘의 추천 퀘스트 및 커뮤니티 하이라이트 -->
    <div class="extra-section">
        <div class="section-title"><span>🌟 오늘의 추천 퀘스트 & 소식</span></div>
        <div class="dashboard-grid">
            <div class="full-card card-flex-col">
                <div>
                    <div class="card-header-flex">
                        <span class="badge">추천 챌린지</span>
                        <span class="desc text-accent-bold">+120 XP</span>
                    </div>
                    <div class="card-title-gap subtitle">광안리에서 일몰 사진 남기기</div>
                    <p class="desc">해 질 무렵 바다를 바라보며 마음에 드는 사진 한 장을 남겨보세요.</p>
                </div>
                <a href="${pageContext.request.contextPath}/quest/quest_main.jsp" class="detail-btn">퀘스트 탐색하러 가기</a>
            </div>

            <div class="full-card card-flex-col">
                <div>
                    <div class="card-header-flex">
                        <span class="badge orange">커뮤니티 피드</span>
                        <span class="desc">10분 전</span>
                    </div>
                    <div class="card-title-gap subtitle">광안대교 야경 보면서 산책하기 완료! ✨</div>
                    <p class="desc">"밤바다가 너무 예쁘네요. 다들 오늘 산책 어떠신가요?" - 여행러버</p>
                </div>
                <a href="${pageContext.request.contextPath}/community/community_main.jsp" class="detail-btn btn-community">커뮤니티 구경하기</a>
            </div>
        </div>
    </div>
</main>

<!-- 공통 삭제 확인 Confirm Modal -->
<div class="modal-backdrop" id="deleteModal">
    <div class="modal">
        <div class="modal-title" id="modalTitle">항목 삭제</div>
        <div class="modal-desc" id="modalDesc">정말 이 항목을 삭제하시겠습니까?</div>
        <div class="modal-btns">
            <button class="modal-btn btn-cancel" onclick="closeDeleteModal()">취소</button>
            <button class="modal-btn btn-confirm" onclick="confirmDelete()">삭제하기</button>
        </div>
    </div>
</div>

<script>
    var currentTargetType = '';

    function openDeleteModal(type, name) {
        currentTargetType = type;
        document.getElementById('modalTitle').innerText = type + ' 삭제';
        document.getElementById('modalDesc').innerText = '"' + name + '" ' + type + '을(를) 정말 삭제하시겠습니까? 관련 데이터가 모두 제거됩니다.';
        document.getElementById('deleteModal').classList.add('show');
    }

    function closeDeleteModal() {
        document.getElementById('deleteModal').classList.remove('show');
    }

    function confirmDelete() {
        alert(currentTargetType + '이(가) 성공적으로 삭제되었습니다.');
        closeDeleteModal();
    }

    var isEmpty = false;
    function toggleEmptyState() {
        isEmpty = !isEmpty;
        var tripContent = document.getElementById('trip-content');
        var routineContent = document.getElementById('routine-content');

        if (isEmpty) {
            tripContent.innerHTML = '<div class="empty-state">등록된 여행이 없습니다.<br>첫 여행을 만들어보세요!</div>';
            routineContent.innerHTML = '<div class="empty-state">활성 루틴이 없습니다.<br>새 루틴을 추가해보세요!</div>';
        } else {
            location.reload();
        }
    }
</script>

</body>
</html>