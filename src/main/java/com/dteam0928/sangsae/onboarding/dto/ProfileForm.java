package com.dteam0928.sangsae.onboarding.dto;

import com.dteam0928.sangsae.user.model.InterestCategory;

import java.util.List;

/** O-02 화면 렌더링용 데이터 (JSP 에서 ${form.nickname}, ${form.categories}) */
public class ProfileForm {

    private String nickname;
    private List<InterestCategory> categories;
    private List<Short> selectedIds;
    private int maxInterestCount;

    public ProfileForm() {
    }

    public ProfileForm(String nickname, List<InterestCategory> categories, List<Short> selectedIds,
                       int maxInterestCount) {
        this.nickname = nickname;
        this.categories = categories;
        this.selectedIds = selectedIds;
        this.maxInterestCount = maxInterestCount;
    }

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }

    public List<InterestCategory> getCategories() { return categories; }
    public void setCategories(List<InterestCategory> categories) { this.categories = categories; }

    public List<Short> getSelectedIds() { return selectedIds; }
    public void setSelectedIds(List<Short> selectedIds) { this.selectedIds = selectedIds; }

    public int getMaxInterestCount() { return maxInterestCount; }
    public void setMaxInterestCount(int maxInterestCount) { this.maxInterestCount = maxInterestCount; }

    /** JSP 에서 칩 선택 상태 표시: ${form.isSelected(c.id)} */
    public boolean isSelected(Short id) {
        return selectedIds != null && selectedIds.contains(id);
    }
}
