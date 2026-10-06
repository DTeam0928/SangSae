package com.dteam0928.sangsae.quest.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_quests")
public class UserQuest {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id")
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "trip_id")
	private Long tripId;

	@Column(name = "origin_template_id")
	private Long originTemplateId;

	@Column(name = "place_id")
	private Long placeId;

	@Column(name = "title", nullable = false, length = 150)
	private String title;

	@Column(name = "scheduled_date", nullable = false)
	private LocalDate scheduledDate;

	@Column(name = "target_time")
	private LocalTime targetTime;

	@Column(name = "auth_type", nullable = false, length = 20)
	private String authType;

	@Column(name = "status", nullable = false, length = 20)
	private String status;

	@Column(name = "reward_xp", nullable = false)
	private int rewardXp;

	@Column(name = "memo", length = 255)
	private String memo;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "completed_at")
	private LocalDateTime completedAt;

	@Column(name = "auth_radius_m")
	private Integer authRadiusM;
    
    public UserQuest() {}

	public UserQuest(Long id, Long userId, Long tripId, Long originTemplateId, Long placeId, String title,
			LocalDate scheduledDate, LocalTime targetTime, String authType, String status, int rewardXp, String memo,
			LocalDateTime createdAt, LocalDateTime completedAt, Integer authRadiusM) {
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
		this.authRadiusM = authRadiusM;
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

	public Integer getAuthRadiusM() {
		return authRadiusM;
	}

	public void setAuthRadiusM(Integer authRadiusM) {
		this.authRadiusM = authRadiusM;
	}
}