package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import org.locationtech.jts.geom.Point;

@Entity
@Table(name = "quest_logs")
public class QuestLog {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "user_quest_id", nullable = false)
	private Long userQuestId;

	@Column(name = "completed_at", nullable = false)
	private LocalDateTime completedAt;

	@Column(name = "auth_type", nullable = false, length = 20)
	private String authType;

	@Column(name = "authenticated_location", columnDefinition = "POINT")
	private Point authenticatedLocation;

	@Column(name = "proof_image_url", length = 500)
	private String proofImageUrl;

	@Column(name = "proof_text", columnDefinition = "TEXT")
	private String proofText;

	@Column(name = "earned_xp", nullable = false)
	private int earnedXp;
    
    public QuestLog() {}

	public QuestLog(Long id, Long userId, Long userQuestId, LocalDateTime completedAt, String authType,
			Point authenticatedLocation, String proofImageUrl, String proofText, int earnedXp) {
		super();
		this.id = id;
		this.userId = userId;
		this.userQuestId = userQuestId;
		this.completedAt = completedAt;
		this.authType = authType;
		this.authenticatedLocation = authenticatedLocation;
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

	public Point getAuthenticatedLocation() {
		return authenticatedLocation;
	}

	public void setAuthenticatedLocation(Point authenticatedLocation) {
		this.authenticatedLocation = authenticatedLocation;
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
