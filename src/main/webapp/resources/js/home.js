/**
 * 홈 대시보드 전용 스크립트 (home.js)
 */

var currentTargetType = '';
var isEmpty = false;

// 삭제 모달 오픈 함수
function openDeleteModal(type, name) {
    currentTargetType = type;
    var modalTitle = document.getElementById('modalTitle');
    var modalDesc = document.getElementById('modalDesc');
    var deleteModal = document.getElementById('deleteModal');

    if (modalTitle) {
        modalTitle.innerText = type + ' 삭제';
    }
    if (modalDesc) {
        modalDesc.innerText = '"' + name + '" ' + type + '을(를) 정말 삭제하시겠습니까? 관련 데이터가 모두 제거됩니다.';
    }
    if (deleteModal) {
        deleteModal.classList.add('show');
    }
}

// 삭제 모달 닫기 함수
function closeDeleteModal() {
    var deleteModal = document.getElementById('deleteModal');
    if (deleteModal) {
        deleteModal.classList.remove('show');
    }
}

// 삭제 확정 처리
function confirmDelete() {
    alert(currentTargetType + '이(가) 성공적으로 삭제되었습니다.');
    closeDeleteModal();
}

// 빈 상태(Empty State) 테스트용 토글 함수
function toggleEmptyState() {
    isEmpty = !isEmpty;
    var tripContent = document.getElementById('trip-content');
    var routineContent = document.getElementById('routine-content');

    if (isEmpty) {
        if (tripContent) {
            tripContent.innerHTML = '<div class="empty-state">등록된 여행이 없습니다.<br>첫 여행을 만들어보세요!</div>';
        }
        if (routineContent) {
            routineContent.innerHTML = '<div class="empty-state">활성 루틴이 없습니다.<br>새 루틴을 추가해보세요!</div>';
        }
    } else {
        location.reload();
    }
}