package com.dteam0928.sangsae.common.exception;

/**
 * 비즈니스 오류 목록.
 *
 *   status  : AJAX 응답 시 HTTP 상태코드
 *   field   : 화면에서 인라인 에러를 붙일 입력 필드 이름 (null 이면 폼 전체 에러)</li>
 *   message : 사용자 안내 문구. {0}, {1} 자리에 인자가 들어간다
 * 
 * 문구를 바꾸고 싶으면 이 파일만 고치면 된다.
 */
public enum ErrorCode {

    // 공통
    INVALID_INPUT(400, null, "입력값을 확인해 주세요."),
    UNAUTHORIZED(401, null, "로그인이 필요합니다."),
    INTERNAL_ERROR(500, null, "일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    NOT_FOUND(404, null, "요청한 정보를 찾을 수 없습니다."),

    // 이메일
    EMAIL_REQUIRED(400, "email", "이메일을 입력해 주세요."),
    EMAIL_INVALID(400, "email", "올바른 이메일 형식이 아닙니다."),
    EMAIL_DUPLICATED(409, "email", "이미 가입된 이메일입니다."),
    EMAIL_NOT_CHECKED(400, "email", "이메일 중복확인을 해 주세요."),

    // 비밀번호
    PASSWORD_REQUIRED(400, "password", "비밀번호를 입력해 주세요."),
    PASSWORD_POLICY(400, "password", "비밀번호는 영문과 숫자를 포함해 {0}~{1}자로 입력해 주세요."),
    PASSWORD_MISMATCH(400, "passwordConfirm", "비밀번호가 일치하지 않습니다."),

    // 로그인
    LOGIN_FAILED(401, null, "이메일 또는 비밀번호가 올바르지 않습니다."),
    SOCIAL_ACCOUNT_EMAIL(409, null, "{0} 계정으로 가입된 이메일입니다. {0} 로그인을 이용해 주세요."),
    ACCOUNT_SUSPENDED(403, null, "이용이 정지된 계정입니다. 고객센터로 문의해 주세요."),
    ACCOUNT_WITHDRAWN(403, null, "탈퇴한 계정입니다."),

    // 소셜 로그인
    OAUTH_PROVIDER_UNSUPPORTED(404, null, "지원하지 않는 로그인 방식입니다."),
    OAUTH_PROVIDER_DISABLED(503, null, "{0} 로그인은 아직 준비 중입니다."),
    OAUTH_CANCELED(400, null, "{0} 로그인이 취소되었습니다."),
    OAUTH_STATE_MISMATCH(400, null, "로그인 요청이 만료되었거나 올바르지 않습니다. 다시 시도해 주세요."),
    OAUTH_FAILED(502, null, "{0} 로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해 주세요."),
    OAUTH_EMAIL_DUPLICATED(409, null, "이미 가입된 이메일입니다. {0}(으)로 로그인해 주세요."),

    // 온보딩
    NICKNAME_REQUIRED(400, "nickname", "필수 입력 항목입니다."),
    NICKNAME_LENGTH(400, "nickname", "닉네임은 {0}~{1}자로 입력해 주세요."),
    NICKNAME_DUPLICATED(409, "nickname", "이미 사용 중인 닉네임입니다."),
    INTEREST_REQUIRED(400, "interests", "최소 1개 이상 선택해주세요."),
    INTEREST_LIMIT(400, "interests", "관심사는 최대 {0}개까지 선택할 수 있습니다."),
    INTEREST_INVALID(400, "interests", "선택할 수 없는 관심사가 포함되어 있습니다."),
    LOCATION_INVALID(400, "location", "위치 정보가 올바르지 않습니다."),

    // 루틴 탐색
    ROUTINE_NOT_FOUND(404, null, "루틴을 찾을 수 없습니다."),
    PLACE_NOT_FOUND(404, "place", "장소를 찾을 수 없습니다."),
    PLACE_INVALID(400, "place", "장소 정보가 올바르지 않습니다."),
    PLACE_LIMIT(400, "place", "자주 가는 장소는 최대 {0}개까지 등록할 수 있습니다."),

     // 루틴 관리
    ROUTINE_TITLE_INVALID(400, "title", "루틴 이름을 {0}자 이내로 입력해 주세요."),
    ROUTINE_PERIOD_INVALID(400, "endDate", "운영 기간을 확인해 주세요. 종료일은 시작일 이후여야 합니다."),
    ROUTINE_CATEGORY_INVALID(400, "categories", "공개 루틴은 카테고리를 1~{0}개 선택해 주세요."),
    ROUTINE_TAG_INVALID(400, "tags", "태그는 {0}개까지, 각 {1}자 이내로 입력해 주세요."),
    ROUTINE_DESCRIPTION_INVALID(400, "description", "설명은 {0}자 이내로 입력해 주세요."),
    ROUTINE_VISIBILITY_LOCKED(400, "visibility", "탐색 탭에서 추가한 루틴은 공개할 수 없습니다."),
    TASK_NOT_FOUND(404, null, "Task를 찾을 수 없습니다."),
    TASK_CONTENT_INVALID(400, "content", "루틴 내용을 {0}자 이내로 입력해 주세요."),
    TASK_TIME_INVALID(400, "time", "시간을 입력해 주세요."),
    TASK_CATEGORY_INVALID(400, "category", "카테고리를 선택해 주세요."),
    TASK_REPEAT_INVALID(400, "repeatType", "반복 설정을 확인해 주세요. 요일 반복은 요일을, 1회성은 운영 기간 안의 날짜를 선택해 주세요."),
    TASK_ALERT_INVALID(400, "alertTime", "알림 시간을 확인해 주세요."),
    TASK_MEMO_INVALID(400, "memo", "메모는 {0}자 이내로 입력해 주세요.");

    private final int status;
    private final String field;
    private final String message;

    ErrorCode(int status, String field, String message) {
        this.status = status;
        this.field = field;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getField() {
        return field;
    }

    public String getMessage() {
        return message;
    }
}
