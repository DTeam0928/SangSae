package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_quest_bookmarks")
@IdClass(UserQuestBookmarkId.class)//2개의 복합 PK를 구분하기 위해 추가
public class UserQuestBookmark {
	@Id
	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Id
	@Column(name = "quest_template_id", nullable = false)
	private Long questTemplateId;

	@Column(name = "folder_name", nullable = false, length = 50)
	private String folderName;

	@Column(name = "created_at", nullable = false)
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
