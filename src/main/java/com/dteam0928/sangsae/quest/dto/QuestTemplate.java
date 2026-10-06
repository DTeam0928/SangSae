package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "quest_templates")
public class QuestTemplate {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;
	
	@Column(name = "creator_id")
	private Long creatorId;

	@Column(name = "place_id")
	private Long placeId;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "description", columnDefinition = "TEXT")
	private String description;

	@Column(name = "category", nullable = false, length = 50)
	private String category;

	@Column(name = "estimated_minutes", nullable = false)
	private int estimatedMinutes;

	@Column(name = "reward_xp", nullable = false)
	private int rewardXp;

	@Column(name = "auth_type", nullable = false, length = 20)
	private String authType;

	@Column(name = "visibility", nullable = false, length = 20)
	private String visibility;

	@Column(name = "thumbnail_url", length = 500)
	private String thumbnailUrl;

	@Column(name = "bookmark_count", nullable = false)
	private int bookmarkCount;

	@Column(name = "completion_count", nullable = false)
	private Integer completionCount;
	
	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;
	
	public QuestTemplate() {}

	public QuestTemplate(Long id, Long creatorId, Long placeId, String title, String description, String category,
			int estimatedMinutes, int rewardXp, String authType, String visibility, String thumbnailUrl,
			int bookmarkCount, Integer completionCount, LocalDateTime createdAt) {
		super();
		this.id = id;
		this.creatorId = creatorId;
		this.placeId = placeId;
		this.title = title;
		this.description = description;
		this.category = category;
		this.estimatedMinutes = estimatedMinutes;
		this.rewardXp = rewardXp;
		this.authType = authType;
		this.visibility = visibility;
		this.thumbnailUrl = thumbnailUrl;
		this.bookmarkCount = bookmarkCount;
		this.completionCount = completionCount;
		this.createdAt = createdAt;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getCreatorId() {
		return creatorId;
	}

	public void setCreatorId(Long creatorId) {
		this.creatorId = creatorId;
	}

	public Long getPlaceId() {
		return placeId;
	}

	public void setPlaceId(Long placeId) {
		this.placeId = placeId;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getCategory() {
		return category;
	}

	public void setCategory(String category) {
		this.category = category;
	}

	public int getEstimatedMinutes() {
		return estimatedMinutes;
	}

	public void setEstimatedMinutes(int estimatedMinutes) {
		this.estimatedMinutes = estimatedMinutes;
	}

	public int getRewardXp() {
		return rewardXp;
	}

	public void setRewardXp(int rewardXp) {
		this.rewardXp = rewardXp;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getVisibility() {
		return visibility;
	}

	public void setVisibility(String visibility) {
		this.visibility = visibility;
	}

	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	public int getBookmarkCount() {
		return bookmarkCount;
	}

	public void setBookmarkCount(int bookmarkCount) {
		this.bookmarkCount = bookmarkCount;
	}
	
	public Integer getCompletionCount() {
		return completionCount;
	}

	public void setCompletionCount(Integer completionCount) {
		this.completionCount = completionCount;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
