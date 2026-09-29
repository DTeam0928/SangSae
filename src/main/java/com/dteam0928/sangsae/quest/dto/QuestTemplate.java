package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDateTime;

public class QuestTemplate {
	private Long id;
	private Long creatorId;
	private Long placeId;
	private String title;
	private String description;
	private String category;
	private int estimatedMinutes;
	private int rewardXp;
	private String authType;
	private String visibility;
	private String thumbnailUrl;
	private int bookmarkCount;
	private LocalDateTime createdAt;
	
	public QuestTemplate() {}

	public QuestTemplate(Long id, Long creatorId, Long placeId, String title, String description, String category,
			int estimatedMinutes, int rewardXp, String authType, String visibility, String thumbnailUrl,
			int bookmarkCount, LocalDateTime createdAt) {
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

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}
}
