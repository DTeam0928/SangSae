package com.dteam0928.sangsae.quest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class UserQuest {
    private Long id;
    private Long userId;
    private Long tripId;
    private Long originTemplateId;
    private Long placeId;
    private String title;
    private LocalDate scheduledDate;
    private LocalTime targetTime;
    private String authType;
    private String status;
    private int rewardXp;
    private String memo;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    
    public UserQuest() {}

	public UserQuest(Long id, Long userId, Long tripId, Long originTemplateId, Long placeId, String title,
			LocalDate scheduledDate, LocalTime targetTime, String authType, String status, int rewardXp, String memo,
			LocalDateTime createdAt, LocalDateTime completedAt) {
		super();
		this.id = id;
		this.userId = userId;
		this.tripId = tripId;
		this.originTemplateId = originTemplateId;
		this.placeId = placeId;
		this.title = title;
		this.scheduledDate = scheduledDate;
		this.targetTime = targetTime;
		this.authType = authType;
		this.status = status;
		this.rewardXp = rewardXp;
		this.memo = memo;
		this.createdAt = createdAt;
		this.completedAt = completedAt;
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

	public Long getTripId() {
		return tripId;
	}

	public void setTripId(Long tripId) {
		this.tripId = tripId;
	}

	public Long getOriginTemplateId() {
		return originTemplateId;
	}

	public void setOriginTemplateId(Long originTemplateId) {
		this.originTemplateId = originTemplateId;
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

	public LocalDate getScheduledDate() {
		return scheduledDate;
	}

	public void setScheduledDate(LocalDate scheduledDate) {
		this.scheduledDate = scheduledDate;
	}

	public LocalTime getTargetTime() {
		return targetTime;
	}

	public void setTargetTime(LocalTime targetTime) {
		this.targetTime = targetTime;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public int getRewardXp() {
		return rewardXp;
	}

	public void setRewardXp(int rewardXp) {
		this.rewardXp = rewardXp;
	}

	public String getMemo() {
		return memo;
	}

	public void setMemo(String memo) {
		this.memo = memo;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getCompletedAt() {
		return completedAt;
	}

	public void setCompletedAt(LocalDateTime completedAt) {
		this.completedAt = completedAt;
	}
}