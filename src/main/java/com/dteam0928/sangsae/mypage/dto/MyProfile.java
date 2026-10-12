package com.dteam0928.sangsae.mypage.dto;

import java.util.List;

/**
 * 마이페이지 프로필 (/api/me/* 응답)
 *
 * @param provider            로그인 수단 LOCAL / GOOGLE / X / APPLE
 * @param providerName        화면 표시용 이름: "이메일" / "Google" ...
 * @param nicknameMaxLength   닉네임 입력칸 maxlength
 * @param interests           전체 관심사 칩 (표시 순서대로, selected = 내가 고른 것)
 * @param selectedInterestIds 내가 고른 관심사 id
 * @param maxInterestCount    관심사 최대 선택 수
 * @param locationPermitted   위치 권한을 허용하고 좌표를 저장했는지 (탐색 탭 추천 기준 위치로 쓰임)
 */
public record MyProfile(String email, String provider, String providerName, String nickname,
                        int nicknameMaxLength, List<Interest> interests, List<Short> selectedInterestIds,
                        int maxInterestCount, boolean locationPermitted) {

    /** 관심사 칩 하나. name 은 '#맛집' 처럼 # 포함 */
    public record Interest(short id, String code, String name, boolean selected) {
    }
}
