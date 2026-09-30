package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

public class UserQuestBookmark {

    private Long userId;
    private Long questTemplateId;
    private String folderName;
    private LocalDateTime createdAt;

    public UserQuestBookmark() {}

    public UserQuestBookmark(Long userId, Long questTemplateId, String folderName, LocalDateTime createdAt) {
        this.userId = userId;
        this.questTemplateId = questTemplateId;
        this.folderName = folderName;
        this.createdAt = createdAt;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getQuestTemplateId() {
        return questTemplateId;
    }

    public void setQuestTemplateId(Long questTemplateId) {
        this.questTemplateId = questTemplateId;
    }

    public String getFolderName() {
        return folderName;
    }

    public void setFolderName(String folderName) {
        this.folderName = folderName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
