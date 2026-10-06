package com.dteam0928.sangsae.onboarding.dto;

import java.util.ArrayList;
import java.util.List;

/** O-02 프로필·관심사 폼 */
public class ProfileSetupRequest {

    private String nickname;
    /** 선택한 interest_categories.id 목록 (0~5개) */
    private List<Short> interestIds = new ArrayList<>();

    public ProfileSetupRequest() {
    }

    public ProfileSetupRequest(String nickname, List<Short> interestIds) {
        this.nickname = nickname;
        this.interestIds = interestIds == null ? new ArrayList<>() : interestIds;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public List<Short> getInterestIds() { return interestIds; }
    public void setInterestIds(List<Short> interestIds) { this.interestIds = interestIds; }
}
