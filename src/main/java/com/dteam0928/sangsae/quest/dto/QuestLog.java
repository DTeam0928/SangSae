package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

public class QuestLog {
    private Long id;
    private Long userId;
    private Long userQuestId;
    private LocalDateTime completedAt;
    private String authType;
    // authenticatedLocation은 위치 타입 확정 후 추가
    private String proofImageUrl;
    private String proofText;
    private int earnedXp;
    
    public QuestLog() {}

	public QuestLog(Long id, Long userId, Long userQuestId, LocalDateTime completedAt, String authType,
			String proofImageUrl, String proofText, int earnedXp) {
		super();
		this.id = id;
		this.userId = userId;
		this.userQuestId = userQuestId;
		this.completedAt = completedAt;
		this.authType = authType;
		this.proofImageUrl = proofImageUrl;
		this.proofText = proofText;
		this.earnedXp = earnedXp;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getUserQuestId() {
		return userQuestId;
	}

	public void setUserQuestId(Long userQuestId) {
		this.userQuestId = userQuestId;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getProofImageUrl() {
		return proofImageUrl;
	}

	public void setProofImageUrl(String proofImageUrl) {
		this.proofImageUrl = proofImageUrl;
	}

	public String getProofText() {
		return proofText;
	}

	public void setProofText(String proofText) {
		this.proofText = proofText;
	}

	public int getEarnedXp() {
		return earnedXp;
	}

	public void setEarnedXp(int earnedXp) {
		this.earnedXp = earnedXp;
	}
}
