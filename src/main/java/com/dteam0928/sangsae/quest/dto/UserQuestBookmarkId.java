package com.dteam0928.sangsae.quest.dto;

import java.io.Serializable;
import java.util.Objects;

//UserQuestBookmark에 2개의 PK를 Java에서 하나의 ID 객체(UserQuestBookmarkId)로 표현하기 위한 클래스
public class UserQuestBookmarkId implements Serializable{
	private static final long serialVersionUID = 1L;
	
	private Long userId;
	private Long questTemplateId;
	
	public UserQuestBookmarkId() {}

	public UserQuestBookmarkId(Long userId, Long questTemplateId) {
		this.userId = userId;
		this.questTemplateId = questTemplateId;
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
	
	@Override
	public boolean equals(Object o) {
		if(this == o) return true;
		if(!(o instanceof UserQuestBookmarkId)) return false;
		UserQuestBookmarkId that = (UserQuestBookmarkId) o;
		return Objects.equals(userId, that.userId)
				&& Objects.equals(questTemplateId, that.questTemplateId);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(userId, questTemplateId);
	}
}
